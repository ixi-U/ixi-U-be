package com.ixi_U.chatbot.controller;

import com.ixi_U.chatbot.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class ChatBotController {

    private final static String CONTENT_TYPE = "Content-Type";
    private final static String PRODUCES = "text/event-stream;charset=UTF-8";

    private final ChatBotService chatBotService;

    @GetMapping(value = "/api/chatbot/welcome", produces = PRODUCES)
    public ResponseEntity<Flux<String>> getWelcomeMessage() {

        return ResponseEntity.ok()
                .header(CONTENT_TYPE, PRODUCES)
                .body(chatBotService.getWelcomeMessage());
    }
}