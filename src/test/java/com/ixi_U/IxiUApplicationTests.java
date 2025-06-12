package com.ixi_U;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class IxiUApplicationTests {

    @Container
    private static Neo4jContainer<?> neo4j = new Neo4jContainer<>(
            DockerImageName.parse("neo4j:5.24"))
            .withAdminPassword("1q2w3e4r");

    @DynamicPropertySource
    static void overrideNeo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "1q2w3e4r");
    }

    @Test
    void contextLoads() {
    }
}