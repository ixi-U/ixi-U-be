package com.ixi_U.chatbot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.neo4j.Neo4jChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatBotConfig {

    private final Neo4jChatMemoryRepository neo4jChatMemoryRepository;

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
