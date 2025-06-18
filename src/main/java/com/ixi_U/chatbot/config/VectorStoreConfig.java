package com.ixi_U.chatbot.config;

import org.neo4j.driver.Driver;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore forbiddenVectorStore(Driver driver, EmbeddingModel embeddingModel) {

        return Neo4jVectorStore.builder(driver, embeddingModel)
                .databaseName("neo4j")
                .indexName("embedded-forbidden-index")
                .label("EmbeddedForbidden")
                .embeddingDimension(1536)
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
                .embeddingDimension(1536)
                .distanceType(Neo4jVectorStore.Neo4jDistanceType.COSINE)
                .initializeSchema(true)
                .build();
    }
}
