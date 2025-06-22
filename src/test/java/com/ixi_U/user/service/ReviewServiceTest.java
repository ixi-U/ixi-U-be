package com.ixi_U.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.forbiddenWord.ReviewFilter;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.request.CreateReviewRequest;
import com.ixi_U.user.dto.response.ShowReviewListResponse;
import com.ixi_U.user.dto.response.ShowReviewResponse;
import com.ixi_U.user.dto.response.ShowReviewStatsResponse;
import com.ixi_U.user.dto.response.ShowReviewSummaryResponse;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.exception.ReviewedException;
import com.ixi_U.user.exception.SubscribedException;
import com.ixi_U.user.repository.ReviewedRepository;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    ReviewService reviewService;
    @Mock
    ReviewedRepository reviewedRepository;

    @Mock
    PlanRepository planRepository;

    @Mock
    UserRepository userRepository;
    @Mock
    SubscribedRepository subscribedRepository;

    @Mock
    ReviewFilter reviewFilter;

    @Nested
    @DisplayName("createReview 메서드는")
    class Describe_createReview {

        @Nested
        @DisplayName("정상 흐름(해피 케이스)에서는")
        class Context_with_success {

            @Test
            @DisplayName("리뷰를 저장한다")
            void it_saves_review() {

                // given
                given(planRepository.findById(any())).willReturn(Optional.of(Plan.of(
                        "플랜1", 20000, 300, 200, 100, 29000,
                        PlanType.ONLINE, "주의사항", 400,
                        0, 100, false, 5, "기타 없음", 5, List.of(), List.of())));
                given(userRepository.findById(any())).willReturn(
                        Optional.of(User.of("testName", "testEmail", "testProvider", 123L,
                                UserRole.ROLE_USER)));
                given(subscribedRepository.existsSubscribeRelation(any(), any())).willReturn(true);
                given(reviewedRepository.existsReviewedRelation(any(), any())).willReturn(false);
                given(reviewFilter.matches(anyString())).willReturn(false);
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

                // when
                reviewService.createReview("user-id",
                        CreateReviewRequest.of("plan-id", 5, "테스트-리뷰"));

                // then
                verify(userRepository, times(1)).save(userCaptor.capture());
                assertThat(userCaptor.getValue().getReviewedHistory()).hasSize(1);
                assertThat(
                        userCaptor.getValue().getReviewedHistory().get(0).getComment()).isEqualTo(
                        "테스트-리뷰");
            }
        }

        @Nested
        @DisplayName("예외 상황에서는")
        class Context_with_failure {

            @Test
            @DisplayName("구독하지 않은 요금제에 대해 리뷰하면 예외를 던진다")
            void it_throws_exception_when_not_subscribed() {

                // given
                given(planRepository.findById(any())).willReturn(
                        Optional.of(Plan.of("플랜1", 20000, 300, 200, 100, 29000,
                                PlanType.ONLINE, "주의사항", 400,
                                0, 100, false, 5, "기타 없음", 5, List.of(), List.of()
                        )));
                given(userRepository.findById(any())).willReturn(
                        Optional.of(User.of("testName", "testEmail", "testProvider", 123L,
                                UserRole.ROLE_USER)));
                given(subscribedRepository.existsSubscribeRelation(any(), any())).willReturn(false);

                // when
                GeneralException ex = assertThrows(GeneralException.class, () -> {
                    reviewService.createReview("user-id",
                            CreateReviewRequest.of("plan-id", 5, "테스트-리뷰"));
                });

                // then
                assertThat(ex.getHttpStatus()).isEqualTo(
                        SubscribedException.PLAN_NOT_SUBSCRIBED.getHttpStatus());
                assertThat(ex.getMessage()).isEqualTo(
                        SubscribedException.PLAN_NOT_SUBSCRIBED.getMessage());
            }

            @Test
            @DisplayName("이미 리뷰한 요금제에 대해 다시 리뷰하면 예외를 던진다")
            void it_throws_exception_when_already_reviewed() {

                // given
                given(planRepository.findById(any())).willReturn(Optional.of(Plan.of(
                        "플랜1", 20000, 300, 200, 100, 29000,
                        PlanType.ONLINE, "주의사항", 400,
                        0, 100, false, 5, "기타 없음", 5, List.of(), List.of())));
                given(userRepository.findById(any())).willReturn(
                        Optional.of(User.of("testName", "testEmail", "testProvider", 123L,
                                UserRole.ROLE_USER)));
                given(subscribedRepository.existsSubscribeRelation(any(), any())).willReturn(true);
                given(reviewedRepository.existsReviewedRelation(any(), any())).willReturn(true);

                // when
                GeneralException ex = assertThrows(GeneralException.class, () -> {
                    reviewService.createReview("user-id",
                            CreateReviewRequest.of("plan-id", 5, "테스트-리뷰"));
                });

                // then
                assertThat(ex.getHttpStatus()).isEqualTo(
                        ReviewedException.REVIEW_ALREADY_EXIST.getHttpStatus());
                assertThat(ex.getMessage()).isEqualTo(
                        ReviewedException.REVIEW_ALREADY_EXIST.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("showReview 메서드는")
    class Describe_showReview {

        @Test
        @DisplayName("요금제에 대한 리뷰 리스트를 반환한다")
        void it_returns_review_list() {

            // given
            String planId = "plan-id";
            Pageable pageable = PageRequest.of(0, 5);

            List<ShowReviewResponse> content = List.of(
                    new ShowReviewResponse(123L,"유저1", 4, "리뷰1",
                            LocalDateTime.of(2025, Month.JUNE, 11, 12, 0)),
                    new ShowReviewResponse(456L,"유저2", 5, "리뷰2",
                            LocalDateTime.of(2025, Month.JUNE, 13, 12, 0))
            );

            Slice<ShowReviewResponse> mockSlice = Mockito.mock(Slice.class);
            given(mockSlice.getContent()).willReturn(content);
            given(mockSlice.hasNext()).willReturn(false);
            given(reviewedRepository.findReviewedByPlanWithPaging(any(), any())).willReturn(
                    mockSlice);

            // when
            ShowReviewListResponse result = reviewService.showReview(planId, pageable);

            // then
            assertThat(result.hasNextPage()).isFalse();
            assertThat(result.reviewResponseList()).hasSize(2);
            assertThat(result.reviewResponseList().get(0).comment()).isEqualTo("리뷰1");
        }
    }

    @Nested
    @DisplayName("showReviewSummary 메서드는")
    class Describe_showReview_summary {

        @Test
        @DisplayName("리뷰 개수와 평점 및 나의 리뷰를 반환한다.")
        void it_returns_review_count_and_rating_and_my_review() {

            // given
            double averagePoint = 3.5;
            int totalCount = 3;
            given(userRepository.findAveragePointAndReviewCount(anyString())).willReturn(
                    ShowReviewStatsResponse.of(averagePoint, totalCount));

            given(reviewedRepository.showMyReview(anyString(),anyString())).willReturn(
                    ShowReviewResponse.of(1L,"jinu",5,"comment",LocalDateTime.now())
            );

            //when
            ShowReviewSummaryResponse showReviewSummary = reviewService.showReviewSummary(
                    "user-id","plan-id");

            //then
            assertThat(showReviewSummary.showReviewStatsResponse().averagePoint()).isEqualTo(averagePoint);
            assertThat(showReviewSummary.showReviewStatsResponse().totalCount()).isEqualTo(totalCount);
            assertThat(showReviewSummary.myReviewResponse().reviewId()).isEqualTo(1L);
        }
    }
}
