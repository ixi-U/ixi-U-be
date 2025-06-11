package com.ixi_U.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.user.dto.request.CreateReviewRequest;
import com.ixi_U.user.dto.response.ShowReviewListResponse;
import com.ixi_U.user.dto.response.ShowReviewResponse;
import com.ixi_U.user.exception.ReviewedException;
import com.ixi_U.user.exception.SubscribedException;
import com.ixi_U.user.service.ReviewService;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@WebMvcTest(value = {ReviewController.class})
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class ReviewControllerTest {

    private static final String REVIEW_URL = "/api/reviews";

    @MockBean
    ReviewService reviewService;
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;


    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void init(RestDocumentationContextProvider restDocumentation) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Nested
    @DisplayName("리뷰 저장 요청은")
    class Describe_createReview {

        @Nested
        @DisplayName("정상적인 요청일 경우")
        class Context_with_valid_request {

            @Test
            @DisplayName("리뷰를 저장하고 201을 반환한다")
            void it_returns_201_created() throws Exception {
                //given
                CreateReviewRequest request = CreateReviewRequest.of("plan-001", 5,
                        "안녕하십니까....저는 이 리뷰를 좋아합니다");

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(document("createReview-success"))
                        .andDo(print());

                // then
                result.andExpect(status().isCreated());
                verify(reviewService, times(1)).createReview("userId", request);
            }

            @DisplayName("별점이 1~5일 경우 저장된다")
            @ParameterizedTest(name = "별점 {0}점")
            @ValueSource(ints = {1, 2, 3, 4, 5})
            void it_saves_review_with_valid_point(int point) throws Exception {
                // given
                doNothing().when(reviewService).createReview(any(), any(CreateReviewRequest.class));

                CreateReviewRequest request = CreateReviewRequest.of("plan-001", point,
                        "1111122222333334444455555");

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print());

                // then
                result.andExpect(status().isCreated());
            }
        }

        @Nested
        @DisplayName("요청이 잘못된 경우")
        class Context_with_invalid_request {

            @Test
            @DisplayName("리뷰 내용이 null이면 400을 반환한다")
            void it_returns_400_if_comment_empty() throws Exception {
                // given
                doNothing().when(reviewService).createReview(any(), any(CreateReviewRequest.class));
                String comment = null;
                CreateReviewRequest request = CreateReviewRequest.of("plan-001", 5, comment);

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(document(
                                "createReview-error-review-content-null"))
                        .andDo(print());

                // then
                result.andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message")
                                .value("comment: comment를 입력해 주세요"));
            }

            @Test
            @DisplayName("리뷰 내용이 20자 미만이면 400을 반환한다")
            void it_returns_400_if_comment_too_short() throws Exception {
                // given
                doNothing().when(reviewService).createReview(any(), any(CreateReviewRequest.class));
                String comment = "아".repeat(19);
                CreateReviewRequest request = CreateReviewRequest.of("plan-001", 5, comment);

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(document(
                                "createReview-error-review-letter-too-short"))
                        .andDo(print());

                // then
                result.andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message")
                                .value("comment: comment는 최소 20자에서 200자까지 입력 가능합니다."));
            }

            @Test
            @DisplayName("리뷰 내용이 200자 초과면 400을 반환한다")
            void it_returns_400_if_comment_too_long() throws Exception {
                // given
                String comment = "아".repeat(201);
                CreateReviewRequest request = CreateReviewRequest.of("plan-001", 5, comment);

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(document(
                                "createReview-error-review-letter-too-long"))
                        .andDo(print());

                // then
                result.andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message")
                                .value("comment: comment는 최소 20자에서 200자까지 입력 가능합니다."));
            }

            @DisplayName("별점이 0 이하이면 400을 반환한다")
            @ParameterizedTest(name = "별점 {0}점")
            @ValueSource(ints = {-1, 0})
            void it_returns_400_if_point_under_zero(int point) throws Exception {
                // given
                CreateReviewRequest request = CreateReviewRequest.of("plan-001", point,
                        "1111122222333334444455555");

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(document(
                                "createReview-error-review-point-under-zero"))
                        .andDo(print());

                // then
                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("point: point는 최소 1점 이상이어야 합니다."));
            }

            @DisplayName("별점이 6 이상이면 400을 반환한다")
            @ParameterizedTest(name = "별점 {0}점")
            @ValueSource(ints = {6, 100})
            void it_returns_400_if_point_over_5(int point) throws Exception {
                // given
                CreateReviewRequest request = CreateReviewRequest.of("plan-001", point,
                        "1111122222333334444455555");

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(document(
                                "createReview-error-review-point-over-6"))
                        .andDo(print());

                // then
                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("point: point는 최대 5점 이하여야 합니다."));
            }

            @Test
            @DisplayName("planId가 비어있으면 400을 반환한다")
            void it_returns_400_if_planId_missing() throws Exception {
                // given
                CreateReviewRequest request = CreateReviewRequest.of("", 5,
                        "1111122222333334444455555");
                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(document(
                                "createReview-error-plan-id-null"))
                        .andDo(print());
                // then
                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("planId: planId를 입력해 주세요"));
            }

            @Test
            @DisplayName("구독하지 않은 요금제에 대해 리뷰하면 400을 반환한다")
            void it_returns_400_when_not_subscribed() throws Exception {
                // given
                doThrow(new GeneralException(SubscribedException.PLAN_NOT_SUBSCRIBED))
                        .when(reviewService).createReview(any(), any());

                CreateReviewRequest request = CreateReviewRequest.of("plan-id", 5,
                        "1111122222333334444455555");

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(document(
                                "createReview-error-when-not-subscribe"))
                        .andDo(print());

                // then
                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(SubscribedException.PLAN_NOT_SUBSCRIBED.getMessage()));
            }

            @Test
            @DisplayName("이미 리뷰한 요금제에 대해 리뷰하면 400을 반환한다")
            void it_returns_400_when_already_reviewed() throws Exception {
                // given
                doThrow(new GeneralException(ReviewedException.REVIEW_ALREADY_EXIST))
                        .when(reviewService).createReview(any(), any());

                CreateReviewRequest request = CreateReviewRequest.of("plan-id", 5,
                        "1111122222333334444455555");

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(document(
                                "createReview-error-when-already-reviewed"))
                        .andDo(print());

                // then
                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(ReviewedException.REVIEW_ALREADY_EXIST.getMessage()));
            }
        }
    }

    @Nested
    @DisplayName("리뷰 목록 조회 요청은")
    class Describe_showReviewList {

        @Test
        @DisplayName("리뷰 목록과 hasNext를 반환한다")
        void it_returns_review_list_and_hasNext() throws Exception {
            // given
            List<ShowReviewResponse> content = List.of(
                    new ShowReviewResponse("유저1", 5, "좋았어요",
                            LocalDateTime.of(2025, Month.JUNE, 11, 12, 0)),
                    new ShowReviewResponse("유저2", 3, "괜찮아요",
                            LocalDateTime.of(2025, Month.JUNE, 13, 12, 0))
            );

            ShowReviewListResponse response = ShowReviewListResponse.of(content, false);

            given(reviewService.showReview(any(), any())).willReturn(response);

            // when
            ResultActions result = mockMvc.perform(
                            get(REVIEW_URL)
                                    .with(csrf())
                                    .param("planId", "plan-001")
                                    .param("page", "0")
                                    .param("size", "5")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(document(
                            "showReview-success"))
                    .andDo(print());

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.hasNextPage").value(false))
                    .andExpect(jsonPath("$.reviewResponseList").isArray())
                    .andExpect(jsonPath("$.reviewResponseList.length()").value(2))
                    .andExpect(jsonPath("$.reviewResponseList[0].comment").value("좋았어요"))
                    .andExpect(jsonPath("$.reviewResponseList[0].userName").value("유저1"))
                    .andExpect(jsonPath("$.reviewResponseList[0].point").value(5))
                    .andExpect(jsonPath("$.reviewResponseList[0].createdAt").value(
                            "2025-06-11T12:00:00"))
                    .andExpect(jsonPath("$.reviewResponseList[1].createdAt").value(
                            "2025-06-13T12:00:00"));
        }
    }


}