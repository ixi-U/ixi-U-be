package com.ixi_U.chatbot.advisor;


import com.ixi_U.chatbot.exception.ChatBotException;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.forbiddenWord.ChatbotFilter;
import java.time.Duration;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
@Component
@RequiredArgsConstructor
public class ForbiddenWordAdvisor implements CallAdvisor {

    private final ChatbotFilter chatbotFilter;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest,
            CallAdvisorChain callAdvisorChain) {

        if(chatbotFilter.matches(chatClientRequest.prompt().getUserMessage().getText())){

            throw new GeneralException(ChatBotException.CHAT_BOT_FORBIDDEN_WORD_DETECT);
        }

        return callAdvisorChain.nextCall(chatClientRequest);
    }

    @Override
    public String getName() {
        return "ForBiddenAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
