package com.ixi_U.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import com.ixi_U.user.service.UserService;
import java.time.LocalDateTime;
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
    @DisplayName("createdAt과 updatedAT에 값이 정상적으로 들어가는지 확인")
    void testCreatedAndModifiedDateAreSet() {
        // 엔티티 생성 및 저장
        User user = userRepository.save(User.of("홍길동", "hong@example.com", "KAKAO"));
        User savedUser = userRepository.save(user);

        // 저장 후 createdAt, updatedAt 값이 null이 아닌지 확인
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        // 엔티티 수정 후 updatedAt이 변경되는지 확인
        LocalDateTime oldUpdatedAt = savedUser.getUpdatedAt();
        // user 정보 수정 (예: 닉네임 변경 등)
        savedUser = userService.changeName(savedUser.getId(), "new 홍길동");

        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser.getUpdatedAt()).isAfterOrEqualTo(oldUpdatedAt);
    }
}
