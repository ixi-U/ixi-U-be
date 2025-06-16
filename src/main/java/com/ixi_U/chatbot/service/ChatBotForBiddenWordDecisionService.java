package com.ixi_U.chatbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatBotForBiddenWordDecisionService {

    @Qualifier("decisionForbiddenWordsClient")
    private final ChatClient decisionForbiddenWords;

    public boolean isForbidden(String sentence) {

        String response = decisionForbiddenWords.prompt(sentence)
                .call()
                .content()
                .trim();

        return response.equalsIgnoreCase("예");
    }
}
