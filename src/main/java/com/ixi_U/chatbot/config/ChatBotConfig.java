package com.ixi_U.chatbot.config;

import com.ixi_U.chatbot.advisor.ForbiddenWordAdvisor;
import com.ixi_U.chatbot.tool.RecommendTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class ChatBotConfig {

    private static final String BASE_PATH = "classpath:/prompts";
    private static final String EXTENSION = ".txt";
    private static final String EMBEDDING_PROMPT = BASE_PATH + "/embedding-prompt" + EXTENSION;
    private static final String RECOMMEND_PROMPT = BASE_PATH + "/recommending-prompt" + EXTENSION;
    private static final String FILTER_EXPRESSION_PROMPT = BASE_PATH + "/filter-expression-prompt" + EXTENSION;

    private final Neo4jChatMemoryRepository neo4jChatMemoryRepository;
    private final ResourceLoader resourceLoader;

    private String loadPrompt(String path) {

        try {
            Resource resource = resourceLoader.getResource(path);
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {

            throw new RuntimeException("프롬프트 파일 로드 실패 : " + path, e);
        }
    }

    @Bean
    public ChatClient descriptionClient(ChatClient.Builder chatClientBuilder) {

        String prompt = loadPrompt(EMBEDDING_PROMPT);

        return chatClientBuilder
                .defaultSystem(prompt)
                .build();
    }

    /**
     * Recommend Build
     */
    @Bean
    public ChatClient recommendClient(ChatClient.Builder chatClientBuilder, RecommendTool recommendTool) {

        String prompt = loadPrompt(RECOMMEND_PROMPT);

        return chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .defaultSystem(prompt)
                .defaultTools(recommendTool)
                .build();
    }

    /**
     * Recommend Build Without ChatMemory
     */
    @Bean
    public ChatClient recommendClientWithoutChatMemory(ChatClient.Builder chatClientBuilder, RecommendTool recommendTool) {

        String prompt = loadPrompt(RECOMMEND_PROMPT);

        return chatClientBuilder
                .defaultSystem(prompt)
                .defaultTools(recommendTool)
                .build();
    }

    /**
     * Filter Expression Build
     */
    @Bean
    public ChatClient filterExpressionClient(ChatClient.Builder chatClientBuilder, ForbiddenWordAdvisor forbiddenWordAdvisor) {

        String prompt = loadPrompt(FILTER_EXPRESSION_PROMPT);

        return chatClientBuilder
                .defaultAdvisors(forbiddenWordAdvisor)
                .defaultSystem(prompt)
                .build();
    }

    /**
     * ChatMemory Build
     */
    @Bean
    public ChatMemory chatMemory() {

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(neo4jChatMemoryRepository)
                .maxMessages(10)
                .build();
    }
}
