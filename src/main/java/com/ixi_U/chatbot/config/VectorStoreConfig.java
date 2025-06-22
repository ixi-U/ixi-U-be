package com.ixi_U.chatbot.config;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore forbiddenVectorStore(Driver driver, EmbeddingModel embeddingModel) {

        log.info("임베딩 모델 차원수 확인 : {}",embeddingModel.dimensions());
        
        return Neo4jVectorStore.builder(driver, embeddingModel)
                .databaseName("neo4j")
                .indexName("embedded-forbidden-index")
                .label("EmbeddedForbidden")
                .distanceType(Neo4jVectorStore.Neo4jDistanceType.COSINE)
                .initializeSchema(true)
                .build();
    }

    @Bean
    public VectorStore planVectorStore(Driver driver, EmbeddingModel embeddingModel) {

        return Neo4jVectorStore.builder(driver, embeddingModel)
                .databaseName("neo4j")
                .indexName("embedded-plan-index")
                .label("EmbeddedPlan")
                .distanceType(Neo4jVectorStore.Neo4jDistanceType.COSINE)
                .initializeSchema(true)
                .build();
    }
}
