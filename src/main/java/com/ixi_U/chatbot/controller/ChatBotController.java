package com.ixi_U.chatbot.controller;

import com.ixi_U.chatbot.aop.SSEEndpoint;
import com.ixi_U.chatbot.dto.RecommendPlanRequest;
import com.ixi_U.chatbot.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatBotController {

    private static final String TEXT_EVENT_STREAM_VALUE = "text/event-stream;charset=UTF-8";

    private final ChatBotService chatBotService;

    @SSEEndpoint
    @GetMapping(value = "/api/chatbot/welcome", produces = TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> getWelcomeMessage() {

        return ResponseEntity.ok().body(chatBotService.getWelcomeMessage());
    }

    @SSEEndpoint
    @PostMapping(value = "/api/chatbot/recommend", produces = TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<List<String>>> recommendPlan(
            String userId,
            @RequestBody RecommendPlanRequest request) {

        return ResponseEntity.ok().body(chatBotService.recommendPlan(userId, request));
    }
}