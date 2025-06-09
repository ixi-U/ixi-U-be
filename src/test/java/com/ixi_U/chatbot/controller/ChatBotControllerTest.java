package com.ixi_U.chatbot.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ixi_U.chatbot.service.ChatBotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

@WebMvcTest(controllers = ChatBotController.class)
class ChatBotControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ChatBotService chatBotService;

    @Nested
    class WhenAPIHasRequested {

        @Test
        @DisplayName("인증/인가된 사용자에게 웰컴 메세지를 응답한다")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        void getWelcomeMessage() throws Exception {

            //given
            String chatBotWelcomeMessage = """        
                    안녕하세요, 고객님! 어떤 요금제를 찾고 계실까요?
                    관심있는 혜택 또는 원하는 조건을 말씀해주시면 최적의 요금제를 안내해드리겠습니다!
                    예시) "넷플릭스 있는 요금제 중 가장 싼 요금제가 뭐야?", "데이터 10기가 이상인 요금제 알려줘"
                    """;

            given(chatBotService.getWelcomeMessage()).willReturn(Flux.just(chatBotWelcomeMessage));

            //when & then
            mockMvc.perform(get("/api/chatbot/welcome")
                            .contentType(MediaType.TEXT_PLAIN_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(content().string(chatBotWelcomeMessage));
        }

        @Test
        @DisplayName("인증/인가 되지않은 사용자는 4xx 에러를 반환한다")
        public void fail() throws Exception {

            //when & then
            mockMvc.perform(get("/api/chatbot/welcome"))
                    .andExpect(status().isUnauthorized());
        }
    }

}