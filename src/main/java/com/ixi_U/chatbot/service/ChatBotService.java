package com.ixi_U.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ixi_U.chatbot.advisor.ForbiddenWordAdvisor;
import com.ixi_U.chatbot.dto.GeneratePlanDescriptionRequest;
import com.ixi_U.chatbot.dto.RecommendPlanRequest;
import com.ixi_U.chatbot.exception.ChatBotException;
import com.ixi_U.chatbot.tool.ToolContextKey;
import com.ixi_U.common.exception.GeneralException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
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
    private static final String GENERAL_ERROR_MESSAGE = """
            죄송합니다. 요금제 추천 서비스에 일시적인 문제가 발생했습니다.
            """;
    private static final String UNEXPECT_ERROR_MESSAGE = """
            죄송합니다. 예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.
            """;


    @Qualifier("descriptionClient")
    private final ChatClient descriptionClient;

    @Qualifier("recommendClient")
    private final ChatClient recommendClient;

    @Qualifier("filterExpressionClient")
    private final ChatClient filterExpressionClient;

    private final ObjectMapper objectMapper;
    private final Neo4jChatMemoryRepository neo4jChatMemoryRepository;

    private final ForbiddenWordAdvisor forbiddenWordAdvisor;

    public Flux<String> getWelcomeMessage() {

        return Flux.fromStream(CHATBOT_WELCOME_MESSAGE.chars()
                        .mapToObj(c -> String.valueOf((char) c))
                )
                .delayElements(Duration.ofMillis(50));
    }

    public Flux<List<String>> recommendPlan(String userId, RecommendPlanRequest request) {

        return Mono.fromCallable(() -> {

                    String llmResult = filterExpressionClient.prompt()
                            .user(request.userQuery())
                            .advisors(forbiddenWordAdvisor)
                            .call()
                            .content();

                    log.info("llmResult = {}", llmResult);

                    if (llmResult == null || llmResult.isBlank()) {

                        throw new GeneralException(ChatBotException.CHAT_BOT_RECOMMENDING_ERROR);
                    }

                    return llmResult;
                })
                .flatMapMany(llmResult ->
                    recommendClient.prompt()
                            .user(request.userQuery())
                            .toolContext(Map.of(ToolContextKey.USER_ID.getKey(), userId, ToolContextKey.FILTER_EXPRESSION.getKey(), llmResult))
                            .stream()
                            .content()
                            .bufferTimeout(5, Duration.ofMillis(50))
                )
                .onErrorResume(GeneralException.class, e -> {

                    log.error("추천 로직 에러 발생", e);

                    return Flux.fromStream(e.getMessage().chars()
                                    .mapToObj(c -> Collections.singletonList(String.valueOf((char) c)))
                            )
                            .delayElements(Duration.ofMillis(50));
                })
                .onErrorResume(Exception.class, e -> {

                    log.error("추천 로직에서 예상치 못한 에러 발생", e);

                    return Flux.fromStream(UNEXPECT_ERROR_MESSAGE.chars()
                                    .mapToObj(c -> Collections.singletonList(String.valueOf((char) c)))
                            )
                            .delayElements(Duration.ofMillis(50));
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

    private void saveMessage(String userId, String userQuery, String assistantResponse){

        saveUserMessage(userId, userQuery);
//        saveAssistantMessage(userId, assistantResponse);

    }

    private void saveUserMessage(String userId, String userQuery){

        List<Message> userChatHistory = neo4jChatMemoryRepository.findByConversationId(userId);

        UserMessage.builder()
                .text(userQuery).build();
//                .metadata(Map.of("planIds",""))
    }
}
