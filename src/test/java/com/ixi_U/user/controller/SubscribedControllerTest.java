package com.ixi_U.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.common.config.FakeNeo4jAuditingConfig;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.common.exception.enums.PlanException;
import com.ixi_U.common.exception.enums.UserException;
import com.ixi_U.user.dto.request.CreateSubscribedRequest;
import com.ixi_U.user.dto.response.ShowSubscribedHistoryResponse;
import com.ixi_U.user.service.SubscribedService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@WebMvcTest(controllers = SubscribedController.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@ActiveProfiles("test")
@Import(FakeNeo4jAuditingConfig.class)
class SubscribedControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscribedService subscribedService;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("utf-8", true))
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Nested
    @DisplayName("요금제 등록 요청은")
    class Describe_updateSubscribed {

        @Test
        @DisplayName("정상 요청일 경우 200 OK를 반환한다")
        void it_returns_200_when_request_is_valid() throws Exception {
            // given
            String userId = "test-user";
            CreateSubscribedRequest request = new CreateSubscribedRequest("plan-1");

            // SecurityContext에 String principal로 직접 주입
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, List.of())
            );
            System.out.println(
                    ">>> 테스트 principal: " + SecurityContextHolder.getContext().getAuthentication()
                            .getPrincipal());

            // when
            ResultActions result = mockMvc.perform(post("/subscribed")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andDo(document("createSubscribed-success"));

            // then
            result.andExpect(status().isOk());
            verify(subscribedService).updateSubscribed(eq(userId), eq(request));

            System.out.println(
                    ">>> 응답 바디: " + result.andReturn().getResponse().getContentAsString());
        }

        @Test
        @DisplayName("planId가 비어있으면 400을 반환한다")
        void it_returns_400_when_planId_is_blank() throws Exception {
            // given
            String userId = "test-user";
            CreateSubscribedRequest invalidRequest = new CreateSubscribedRequest("");

            // SecurityContext에 String principal로 직접 주입
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, List.of())
            );
            System.out.println(
                    ">>> 테스트 principal: " + SecurityContextHolder.getContext().getAuthentication()
                            .getPrincipal());

            // when
            ResultActions result = mockMvc.perform(post("/subscribed")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andDo(document("createSubscribed-error-plan-id-blank"));

            // then
            result.andExpect(status().isBadRequest());
            verifyNoInteractions(subscribedService);

            System.out.println(
                    ">>> 응답 바디: " + result.andReturn().getResponse().getContentAsString());
        }

        @Test
        @DisplayName("존재하지 않는 userId이면 404 반환")
        void it_returns_404_when_user_not_found() throws Exception {
            // given
            String userId = "unknown-user";
            CreateSubscribedRequest request = new CreateSubscribedRequest("plan-1");

            doThrow(new GeneralException(UserException.USER_NOT_FOUND))
                    .when(subscribedService).updateSubscribed(eq(userId), eq(request));

            // SecurityContext에 String principal로 직접 주입
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, List.of())
            );
            System.out.println(
                    ">>> 테스트 principal: " + SecurityContextHolder.getContext().getAuthentication()
                            .getPrincipal());

            // when
            ResultActions result = mockMvc.perform(post("/subscribed")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andDo(document("createSubscribed-error-user-not-found"));

            // then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(UserException.USER_NOT_FOUND.getMessage()));

            System.out.println(
                    ">>> 응답 바디: " + result.andReturn().getResponse().getContentAsString());
        }

        @Test
        @DisplayName("존재하지 않는 planId이면 404 반환")
        void it_returns_404_when_plan_not_found() throws Exception {
            // given
            String userId = "test-user";
            CreateSubscribedRequest request = new CreateSubscribedRequest("invalid-plan");

            doThrow(new GeneralException(PlanException.PLAN_NOT_FOUND))
                    .when(subscribedService).updateSubscribed(eq(userId), eq(request));

            // SecurityContext에 String principal로 직접 주입
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, List.of())
            );
            System.out.println(
                    ">>> 테스트 principal: " + SecurityContextHolder.getContext().getAuthentication()
                            .getPrincipal());

            // when
            ResultActions result = mockMvc.perform(post("/subscribed")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andDo(document("createSubscribed-error-plan-not-found"));

            // then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(PlanException.PLAN_NOT_FOUND.getMessage()));

            System.out.println(
                    ">>> 응답 바디: " + result.andReturn().getResponse().getContentAsString());
        }

        @Test
        @DisplayName("이미 구독 중인 요금제면 400 반환")
        void it_returns_400_when_already_subscribed() throws Exception {
            // given
            String userId = "test-user";
            CreateSubscribedRequest request = new CreateSubscribedRequest("plan-1");

            doThrow(new GeneralException(PlanException.ALREADY_SUBSCRIBED_PLAN))
                    .when(subscribedService).updateSubscribed(eq(userId), eq(request));

            // SecurityContext에 String principal로 직접 주입
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, List.of())
            );
            System.out.println(
                    ">>> 테스트 principal: " + SecurityContextHolder.getContext().getAuthentication()
                            .getPrincipal());

            // when
            ResultActions result = mockMvc.perform(post("/subscribed")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andDo(document("createSubscribed-error-already-subscribed"));

            // then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(PlanException.ALREADY_SUBSCRIBED_PLAN.getMessage()));

            System.out.println(
                    ">>> 응답 바디: " + result.andReturn().getResponse().getContentAsString());
        }
    }

    @Nested
    @DisplayName("구독 히스토리 조회 요청은")
    class Describe_getSubscribedHistory {

        @Test
        @DisplayName("정상적인 userId일 경우 구독 히스토리를 반환한다")
        void it_returns_subscribed_history_when_userId_is_valid() throws Exception {
            // given
            String userId = "user-1";
            List<ShowSubscribedHistoryResponse> responses = List.of(
                    new ShowSubscribedHistoryResponse(1L, "베이직",
                            LocalDateTime.of(2024, 6, 1, 10, 0)),
                    new ShowSubscribedHistoryResponse(2L, "프리미엄",
                            LocalDateTime.of(2024, 6, 2, 11, 0)),
                    new ShowSubscribedHistoryResponse(3L, "스페셜",
                            LocalDateTime.of(2024, 6, 3, 12, 0))
            );

            org.mockito.Mockito.when(subscribedService.findSubscribedHistoryByUserId(userId))
                    .thenReturn(responses);

            // SecurityContext에 String userId를 principal로 직접 넣음
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, List.of())
            );

            // 로그: 테스트 시작 시 principal 출력
            System.out.println(
                    ">>> 테스트 principal: " + SecurityContextHolder.getContext().getAuthentication()
                            .getPrincipal());

            // when
            ResultActions result = mockMvc.perform(get("/subscribed/history")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andDo(document("getSubscribedHistory-success"));

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].planName").value("베이직"));
        }

        @Test
        @DisplayName("존재하지 않는 userId일 경우 404를 반환한다")
        void it_returns_404_when_user_not_found() throws Exception {
            // given
            String userId = "not-found-user";

            doThrow(new GeneralException(UserException.USER_NOT_FOUND))
                    .when(subscribedService).findSubscribedHistoryByUserId(userId);

            // SecurityContext에 String principal로 직접 주입
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, List.of())
            );
            // principal 로그
            System.out.println(
                    ">>> 테스트 principal: " + SecurityContextHolder.getContext().getAuthentication()
                            .getPrincipal());

            // when
            ResultActions result = mockMvc.perform(get("/subscribed/history")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andDo(document("getSubscribedHistory-error-user-not-found"));

            // then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(UserException.USER_NOT_FOUND.getMessage()));

            // 응답 바디 로그
            String responseBody = result.andReturn().getResponse().getContentAsString();
            System.out.println(">>> 응답 바디: " + responseBody);
        }
    }
}
