package com.ixi_U.chatbot.controller;

import static com.ixi_U.util.TestConstants.CHATBOT_WELCOME_MESSAGE;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

import com.ixi_U.chatbot.service.ChatBotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@ActiveProfiles("test")
@WebFluxTest(controllers = ChatBotController.class)
@ExtendWith(RestDocumentationExtension.class)
class ChatBotControllerTest {

    WebTestClient webTestClient;

    @BeforeEach
    void setUp(ApplicationContext applicationContext,
            RestDocumentationContextProvider restDocumentation) {
        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .configureClient()
                .filter(documentationConfiguration(restDocumentation))
                .build();
    }

    @MockBean
    ChatBotService chatBotService;

    @Nested
    class WhenAPIHasRequested {

        @Test
        @DisplayName("인증/인가된 사용자에게 웰컴 메세지를 응답한다")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void getWelcomeMessageTest() {

            //given
            String[] split = CHATBOT_WELCOME_MESSAGE.split("\n");
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
                    .consumeWith(document("getWelcomeMessage"));
        }
    }
}
