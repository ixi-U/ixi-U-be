package com.ixi_U.chatbot.controller;

import com.ixi_U.chatbot.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    /**
     * 챗봇의 환영 메시지를 비동기적으로 반환합니다.
     *
     * @return 환영 메시지의 Flux 스트림
     */
    @GetMapping(value = "/api/chatbot/welcome", produces = "text/plain;charset=UTF-8")
    public Flux<String> getWelcomeMessage() {

        return chatBotService.getWelcomeMessage();
    }
}