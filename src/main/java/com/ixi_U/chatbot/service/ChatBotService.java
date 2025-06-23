package com.ixi_U.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.chatbot.dto.GeneratePlanDescriptionRequest;
import com.ixi_U.chatbot.dto.RecommendPlanRequest;
import com.ixi_U.chatbot.exception.ChatBotException;
import com.ixi_U.chatbot.tool.ToolContextKey;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.user.entity.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
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

    @Qualifier("recommendClientWithoutChatMemory")
    private final ChatClient recommendClientWithoutChatMemory;

    private final ObjectMapper objectMapper;

    public Flux<String> getWelcomeMessage() {

        return Flux.fromStream(CHATBOT_WELCOME_MESSAGE.chars()
                        .mapToObj(c -> String.valueOf((char) c))
                )
                .delayElements(Duration.ofMillis(30));
    }

    public Flux<List<String>> recommendPlan(String userId, RecommendPlanRequest request) {

        return Mono.fromCallable(() -> {

                    String filterExpression = filterExpressionClient.prompt()
                            .user(request.userQuery())
                            .call()
                            .content();

                    log.info("filterExpression = {}", filterExpression);

                    if (filterExpression == null || filterExpression.isBlank()) {

                        throw new GeneralException(ChatBotException.CHAT_BOT_RECOMMENDING_ERROR);
                    }

                    return filterExpression;
                })
                .flatMapMany(filterExpression -> {

                    if (isAnonymousUser(userId)){

                        return recommendClientWithoutChatMemory.prompt()
                                .user(request.userQuery())
                                .toolContext(Map.of(
                                        ToolContextKey.USER_ID.getKey(), userId,
                                        ToolContextKey.FILTER_EXPRESSION.getKey(), filterExpression)
                                )
                                .stream()
                                .content()
                                .bufferTimeout(5, Duration.ofMillis(30));
                    }

                    return recommendClient.prompt()
                            .user(request.userQuery())
                            .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, userId))
                            .toolContext(Map.of(
                                    ToolContextKey.USER_ID.getKey(), userId,
                                    ToolContextKey.FILTER_EXPRESSION.getKey(), filterExpression)
                            )
                            .stream()
                            .content()
                            .bufferTimeout(5, Duration.ofMillis(30));
                })
                .onErrorResume(GeneralException.class, e -> {

                    log.error("추천 로직 에러 발생", e);

                    return generateErrorResponse(e.getMessage());
                })
                .onErrorResume(Exception.class, e -> {

                    log.error("추천 로직에서 예상치 못한 에러 발생", e);

                    return generateErrorResponse(e.getMessage());
                });
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

    private boolean isAnonymousUser(String userId) {

        return userId.equals(UserRole.ROLE_ANONYMOUS.getUserRole());
    }

    private Flux<List<String>> generateErrorResponse(String error) {

        return Flux.fromStream(error.chars()
                        .mapToObj(c -> Collections.singletonList(String.valueOf((char) c)))
                )
                .delayElements(Duration.ofMillis(50));
    }
}
