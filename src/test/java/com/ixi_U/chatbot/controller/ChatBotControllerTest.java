package com.ixi_U.chatbot.controller;

import static com.ixi_U.chatbot.constants.TestConstants.CHATBOT_WELCOME_MESSAGE;
import static org.mockito.BDDMockito.given;

import com.ixi_U.chatbot.service.ChatBotService;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@WebFluxTest(controllers = ChatBotController.class)
class ChatBotControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    ChatBotService chatBotService;

    @Nested
    class WhenAPIHasRequested {

        @Test
        @DisplayName("인증/인가된 사용자에게 웰컴 메세지를 응답한다")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void getWelcomeMessage() {

            //given
            String[] split = CHATBOT_WELCOME_MESSAGE.split("\n");
            given(chatBotService.getWelcomeMessage()).willReturn(
                    Flux.fromArray(split)
            );

            //when & then
            FluxExchangeResult<String> result = webTestClient.get()
                    .uri("/api/chatbot/welcome")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(String.class);

            Flux<String> responseBody = result.getResponseBody();

            StepVerifier.create(responseBody)
                    .expectNextSequence(Arrays.asList(split))
                    .verifyComplete();
        }

        @Test
        @DisplayName("인증/인가 되지않은 사용자는 4xx 에러를 반환한다")
        void unauthorizedUserTest() {

            //when & then
            webTestClient.get()
                    .uri("/api/chatbot/welcome")
                    .exchange()
                    .expectStatus().is4xxClientError();
        }
    }
}
