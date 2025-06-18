package com.ixi_U.forbiddenWord;

import com.ixi_U.chatbot.service.ChatBotForBiddenWordDecisionService;
import com.ixi_U.forbiddenWord.filters.AhoCorasickFilter;
import com.ixi_U.forbiddenWord.filters.ForbiddenWordFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatbotFilter implements ForbiddenWordFilter {

    private final ForbiddenWordLoader forbiddenWordLoader;
    private final AhoCorasickFilter ahoCorasickFilter;
    private final ChatBotForBiddenWordDecisionService llmDecisionService;

    @Override
    public boolean matches(String text) {

        // 1) 기본 contains 검사
        for (String word : forbiddenWordLoader.getForbiddenWords()) {
            if (text.contains(word)) {

                return true;
            }
        }

        // 2) Aho-Corasick 검사
        if (ahoCorasickFilter.matches(text)) {

            return true;
        }

        // 3) LLM 검사
        return llmDecisionService.isForbidden(text);
    }
}
