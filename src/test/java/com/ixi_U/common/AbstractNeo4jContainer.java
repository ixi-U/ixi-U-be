package com.ixi_U.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractNeo4jContainer {

    @Container
    protected static final Neo4jContainer<?> neo4j = new Neo4jContainer<>(
            DockerImageName.parse("neo4j:5.15"))
            .withAdminPassword(System.getenv().getOrDefault("GRAPH_DB_PASSWORD", "testPassword"));

    @DynamicPropertySource
    static void overrideNeo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password",
                () -> System.getenv().getOrDefault("GRAPH_DB_PASSWORD", "testPassword"));
    }
}
