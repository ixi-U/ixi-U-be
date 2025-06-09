package com.ixi_U.chatbot.service;

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
        String chatBotWelcomeMessage = """        
                안녕하세요, 고객님! 어떤 요금제를 찾고 계실까요?
                관심있는 혜택 또는 원하는 조건을 말씀해주시면 최적의 요금제를 안내해드리겠습니다!
                예시) "넷플릭스 있는 요금제 중 가장 싼 요금제가 뭐야?", "데이터 10기가 이상인 요금제 알려줘"
                """;

        //when
        Flux<String> welcomeMessage = chatBotService.getWelcomeMessage();

        //then
        StepVerifier.create(welcomeMessage)
                .expectNext(chatBotWelcomeMessage)
                .verifyComplete();
    }
}