package com.ixi_U.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.request.CreateReviewRequest;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.exception.ReviewedException;
import com.ixi_U.user.exception.SubscribedException;
import com.ixi_U.user.repository.ReviewedRepository;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                given(planRepository.findById(any())).willReturn(Optional.of(Plan.of("플랜1")));
                given(userRepository.findById(any())).willReturn(
                        Optional.of(User.of("testName", "testEmail", "testProvider")));
                given(subscribedRepository.existsSubscribeRelation(any(), any())).willReturn(true);
                given(reviewedRepository.existsReviewedRelation(any(), any())).willReturn(false);

                // when
                reviewService.createReview("user-id",
                        CreateReviewRequest.of("plan-id", 5, "테스트-리뷰"));

                // then
                verify(userRepository, times(1)).save(any(User.class));
            }
        }

        @Nested
        @DisplayName("예외 상황에서는")
        class Context_with_failure {

            @Test
            @DisplayName("구독하지 않은 요금제에 대해 리뷰하면 예외를 던진다")
            void it_throws_exception_when_not_subscribed() {
                // given
                given(planRepository.findById(any())).willReturn(Optional.of(Plan.of("플랜1")));
                given(userRepository.findById(any())).willReturn(
                        Optional.of(User.of("testName", "testEmail", "testProvider")));
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
                given(planRepository.findById(any())).willReturn(Optional.of(Plan.of("플랜1")));
                given(userRepository.findById(any())).willReturn(
                        Optional.of(User.of("testName", "testEmail", "testProvider")));
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
}
