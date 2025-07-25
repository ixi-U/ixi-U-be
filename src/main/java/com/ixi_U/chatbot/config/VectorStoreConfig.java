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
