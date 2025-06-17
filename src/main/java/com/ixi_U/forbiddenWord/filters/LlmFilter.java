package com.ixi_U.forbiddenWord.filters;

import com.ixi_U.chatbot.service.ChatBotForBiddenWordDecisionService;
import com.ixi_U.forbiddenWord.ForbiddenWordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("llmFilter")
public class LlmFilter implements ForbiddenWordFilter {

    private final ChatBotForBiddenWordDecisionService chatBotForBiddenWordDecisionService;

    @Override
    public boolean matches(String text) {

        return chatBotForBiddenWordDecisionService.isForbidden(text);
    }
}
