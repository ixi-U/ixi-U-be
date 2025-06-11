package com.ixi_U.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import com.ixi_U.user.service.UserService;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BaseEntityAuditingTest {

    @Container
    private static final Neo4jContainer<?> neo4j = new Neo4jContainer<>(
            DockerImageName.parse("neo4j:5.15"))
            .withAdminPassword(System.getenv().getOrDefault("GRAPH_DB_PASSWORD", "testPassword"))
            .withReuse(true);

    @Autowired
    UserRepository userRepository;
    @Autowired
    private UserService userService;

    @DynamicPropertySource
    static void overrideNeo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password",
                () -> System.getenv().getOrDefault("GRAPH_DB_PASSWORD", "testPassword"));
    }

    @Test
    @DisplayName("엔티티 저장 시 createdAt, updatedAt이 세팅된다")
    void testCreatedAndUpdatedAtSetOnSave() {
        // given & when
        User savedUser = userRepository.save(User.of("홍길동", "hong@example.com", "KAKAO"));

        // then
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("엔티티 수정 시 updatedAt이 변경된다")
    void testUpdatedAtChangesOnUpdate() {
        // given
        User savedUser = userRepository.save(User.of("홍길동", "hong@example.com", "KAKAO"));
        Instant oldUpdatedAt = savedUser.getUpdatedAt();

        // when
        User updatedUser = userRepository.save(
                userService.changeName(savedUser.getId(), "new 홍길동"));

        // then
        assertThat(updatedUser.getUpdatedAt()).isAfter(oldUpdatedAt);
    }

}
