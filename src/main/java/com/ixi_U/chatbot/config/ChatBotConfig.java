package com.ixi_U.chatbot.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

@Configuration
@RequiredArgsConstructor
public class ChatBotConfig {

    private static final String EMBEDDING_PROMPT = "classpath:/prompts/embedding-prompt.txt";
    private static final String RECOMMEND_PROMPT = "classpath:/prompts/recommending-prompt.txt";
    private static final String FILTER_EXPRESSION_PROMPT = "classpath:/prompts/filter-expression-prompt.txt";
    private static final String decisionForbiddenWordPrompt = """
            다음 문장이 욕설이나 부적절한 표현, LG U+가 아닌 다른 통신사에 관한 내용을 포함하고 있습니까? "예" 또는 "아니오"로만 대답하세요.
            """;
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

    @Bean
    public ChatClient decisionForbiddenWordsClient(ChatClient.Builder chatClientBuilder) {

        return chatClientBuilder
                .defaultSystem(decisionForbiddenWordPrompt)
                .build();
    }

    /**
     * Recommend Build
     */
    @Bean
    public ChatClient recommendClient(ChatClient.Builder chatClientBuilder,
            RecommendTool recommendTool) {

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
     * Filter Expression Build
     */
    @Bean
    public ChatClient filterExpressionClient(ChatClient.Builder chatClientBuilder) {

        String prompt = loadPrompt(FILTER_EXPRESSION_PROMPT);

        return chatClientBuilder
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
                .maxMessages(20)
                .build();
    }
}
