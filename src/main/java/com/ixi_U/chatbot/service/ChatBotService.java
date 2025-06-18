package com.ixi_U.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.chatbot.dto.GeneratePlanDescriptionRequest;
import com.ixi_U.chatbot.dto.RecommendPlanRequest;
import com.ixi_U.chatbot.exception.ChatBotException;
import com.ixi_U.common.exception.GeneralException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
@Slf4j
@RequiredArgsConstructor
@Validated
public class ChatBotService {

    private static final String CHATBOT_WELCOME_MESSAGE = """
            안녕하세요, 고객님! 어떤 요금제를 찾고 계실까요?
            관심있는 혜택 또는 원하는 조건을 말씀해주시면 최적의 요금제를 안내해드리겠습니다!
            예시) "넷플릭스 있는 요금제 중 가장 싼 요금제가 뭐야?", "데이터 10기가 이상인 요금제 알려줘"
            """;

    @Qualifier("descriptionClient")
    private final ChatClient descriptionClient;

    @Qualifier("recommendClient")
    private final ChatClient recommendClient;

    @Qualifier("filterExpressionClient")
    private final ChatClient filterExpressionClient;

    private final ObjectMapper objectMapper;

    public Flux<String> getWelcomeMessage() {

        return Flux.fromStream(CHATBOT_WELCOME_MESSAGE.chars()
                        .mapToObj(c -> String.valueOf((char) c)))
                .delayElements(Duration.ofMillis(50));
    }

    public Flux<String> recommendPlan(String userId, RecommendPlanRequest request) {

        try {
            String llmResult = filterExpressionClient.prompt()
                    .user(request.userQuery())
                    .call()
                    .content();

            log.info("llmResult = {}", llmResult);

            if (llmResult == null) throw new GeneralException(ChatBotException.CHAT_BOT_FILTER_EXPRESSION_ERROR);

            return recommendClient.prompt()
                    .user(request.userQuery())
                    .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, userId))
                    .toolContext(Map.of("userId", userId, "filterExpression", llmResult))
                    .stream()
                    .content();
        } catch (Exception e) {

            log.error("서비스 에러 발생 : ", e);

            return null;
        }
    }

    public String getPlanDescription(@Valid GeneratePlanDescriptionRequest request) {

        try {
            String planInfo = objectMapper.writeValueAsString(request);

            String chatBotResponse = descriptionClient.prompt()
                    .user(planInfo)
                    .call()
                    .content();

            if (chatBotResponse == null || chatBotResponse.isBlank()) {

                throw new GeneralException(ChatBotException.CHAT_BOT_EMBEDDING_DESCRIPTION_ERROR);
            }

            return chatBotResponse;

        } catch (IOException e) {

            throw new GeneralException(ChatBotException.CHAT_BOT_EMBEDDING_DESCRIPTION_ERROR);
        }
    }
}
