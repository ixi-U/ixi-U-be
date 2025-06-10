package com.ixi_U.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.user.dto.request.CreateReviewRequest;
import com.ixi_U.user.service.ReviewService;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@WebMvcTest(value = {ReviewController.class})
@ExtendWith({SpringExtension.class})
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
    public void init() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
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
                // given
                doNothing().when(reviewService).createReview(any(), any(CreateReviewRequest.class));

                CreateReviewRequest request = CreateReviewRequest.of("plan-001", 5,
                        "안녕하십니까....저는 이 리뷰를 좋아합니다");

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
                doNothing().when(reviewService).createReview(any(), any(CreateReviewRequest.class));
                String comment = "아".repeat(201);
                CreateReviewRequest request = CreateReviewRequest.of("plan-001", 5, comment);

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
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
                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("point: point는 최소 1점 이상이어야 합니다."));
            }

            @DisplayName("별점이 6 이상이면 400을 반환한다")
            @ParameterizedTest(name = "별점 {0}점")
            @ValueSource(ints = {6, 100})
            void it_returns_400_if_point_over_5(int point) throws Exception {
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
                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("point: point는 최대 5점 이하여야 합니다."));
            }

            @Test
            @DisplayName("planId가 비어있으면 400을 반환한다")
            void it_returns_400_if_planId_missing() throws Exception {
                // given
                doNothing().when(reviewService).createReview(any(), any(CreateReviewRequest.class));
                CreateReviewRequest request = CreateReviewRequest.of("", 5,
                        "1111122222333334444455555");

                // when
                ResultActions result = mockMvc.perform(post(REVIEW_URL)
                                .with(csrf())
                                .param("userId", "userId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print());

                // then
                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value("planId: planId를 입력해 주세요"));
            }
        }
    }


}