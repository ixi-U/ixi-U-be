package com.ixi_U.chatbot.service;

import static com.ixi_U.chatbot.constants.TestConstants.CHATBOT_WELCOME_MESSAGE;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ChatBotServiceTest {

    @InjectMocks
    ChatBotService chatBotService;

    @Test
    @DisplayName("웰컴 메세지를 응답한다")
    public void getWelcomeMessage() {

        //given

        //when
        Flux<String> welcomeMessage = chatBotService.getWelcomeMessage();

        //then
        StepVerifier.create(welcomeMessage.collectList())
                .expectNextMatches(chars -> String.join("", chars).equals(CHATBOT_WELCOME_MESSAGE))
                .verifyComplete();
    }
}