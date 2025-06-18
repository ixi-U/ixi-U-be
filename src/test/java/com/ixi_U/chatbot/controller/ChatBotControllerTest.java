package com.ixi_U.chatbot.controller;

import com.ixi_U.TestWebFluxSecurityConfig;
import com.ixi_U.chatbot.dto.RecommendPlanRequest;
import com.ixi_U.chatbot.service.ChatBotService;
import com.ixi_U.common.config.SecurityConfig;
import com.ixi_U.util.constants.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

@Import(TestWebFluxSecurityConfig.class)
@ActiveProfiles("test")
@WebFluxTest(controllers = ChatBotController.class)
@ExtendWith(RestDocumentationExtension.class)
class ChatBotControllerTest {

    WebTestClient webTestClient;

    @MockBean
    ChatBotService chatBotService;

    @MockBean
    ChatClient recommendClient;

    @BeforeEach
    void setUp(ApplicationContext applicationContext,
               RestDocumentationContextProvider restDocumentation) {
        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .configureClient()
                .filter(documentationConfiguration(restDocumentation))
                .build();
    }

    @Nested
    class WhenAPIHasRequested {

        @Test
        @WithMockUser(username = "user", roles = "USER")
        @DisplayName("사용자에게 웰컴 메세지를 응답한다")
        void getWelcomeMessageTest() {

            //given
            String[] split = TestConstants.CHATBOT_WELCOME_MESSAGE.split("\n");
            given(chatBotService.getWelcomeMessage()).willReturn(Flux.fromArray(split));

            //when & then
            webTestClient.get()
                    .uri("/api/chatbot/welcome")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                    .expectBodyList(String.class)
                    .contains(split[0])
                    .consumeWith(document("get-welcome-message-success"));
        }

        @Nested
        class WhenRecommendAPIRequested {

            @Test
            @WithMockUser(username = "user", roles = "USER")
            @DisplayName("사용자에게 요금제를 추천해준다")
            public void recommendPlanSuccessTest() {

                //given
                String userQuery = "6만원 이하의 무제한 요금제 추천 해줘";
                String userId = "testUser";
                List<String> assistanceResponse = List.of("너겟 59 요금제를 추천합니다".split(""));
                RecommendPlanRequest recommendPlanRequest = RecommendPlanRequest.create(userQuery);
                given(chatBotService.recommendPlan(userId, recommendPlanRequest)).willReturn(Flux.just(assistanceResponse));

                //when & then
                webTestClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/chatbot/recommend")
                                .queryParam("userId", userId)
                                .build())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(recommendPlanRequest)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType("text/event-stream;charset=UTF-8")
                        .expectBodyList(String.class)
                        .consumeWith(document("recommend-plan-success"));
            }//recommendPlanSuccessTest

            @Test
            @WithMockUser(username = "user", roles = "USER")
            @DisplayName("응답이 없을 경우 실패 응답을 반환한다")
            public void recommendPlanFailTest() throws Exception {

                //given
                RecommendPlanRequest recommendPlanRequest = RecommendPlanRequest.create("6만원 이하의 무제한 요금제 추천 해줘");
                String userId = "testUser";

                given(chatBotService.recommendPlan(userId, recommendPlanRequest)).willReturn(null);

                //when & then
                webTestClient.post()
                        .uri("/api/chatbot/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().is4xxClientError()
                        .expectHeader().contentType("application/json")
                        .expectBodyList(String.class)
                        .consumeWith(document("recommend-plan-fail"));
            }//recommendPlanFailTest
        }// WhenRecommendAPIRequested

    }
}
