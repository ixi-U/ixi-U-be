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

    private static final String PROMPT_PATH = "classpath:/prompts/description.txt";
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

        String prompt = loadPrompt(PROMPT_PATH);

        return chatClientBuilder
                .defaultSystem(prompt)
                .build();
    }

    /**
     * ChatClient Build
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {

        return chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
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
