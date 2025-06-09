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

    /****
     * ChatClient 인스턴스를 생성하여 Spring Bean으로 등록합니다.
     *
     * ChatMemory 기반의 MessageChatMemoryAdvisor를 기본 advisor로 설정하여 ChatClient를 빌드합니다.
     *
     * @return 기본 advisor가 설정된 ChatClient 인스턴스
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
     * Neo4j 기반의 메시지 윈도우 챗 메모리 빈을 생성합니다.
     *
     * 최대 20개의 메시지를 저장하며, 대화 기록은 Neo4jChatMemoryRepository를 통해 영속화됩니다.
     *
     * @return 최대 20개 메시지를 저장하는 MessageWindowChatMemory 인스턴스
     */
    @Bean
    public ChatMemory chatMemory() {

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(neo4jChatMemoryRepository)
                .maxMessages(20)
                .build();
    }
}
