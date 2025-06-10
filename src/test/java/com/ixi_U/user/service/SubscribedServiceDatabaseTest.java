package com.ixi_U.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@DataNeo4jTest
@ActiveProfiles("test")
@Transactional
@Testcontainers
@DisplayName("Testcontainers 기반 Neo4j DB와 실제 연동되는 구독관계 저장 테스트")
class SubscribedServiceDatabaseTest {

    @Container
    private static final Neo4jContainer<?> neo4j = new Neo4jContainer<>(
            DockerImageName.parse("neo4j:5"))
            .withAdminPassword("1q2w3e4r")
            .withReuse(true);
    @Autowired
    UserRepository userRepository;
    @Autowired
    PlanRepository planRepository;

    @DynamicPropertySource
    static void overrideNeo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "1q2w3e4r");
    }

    @Test
    @DisplayName("회원과 요금제 생성 후 실제 SUBSCRIBED 관계가 DB에 저장된다")
    void givenUserAndPlan_whenUpdateSubscribed_thenSubscribedRelationshipIsSaved() {
        // given
        User user = User.of("홍승민", "hong@example.com", "KAKAO");
        user = userRepository.save(user);

        Plan plan = Plan.of("5G 요금제");
        plan = planRepository.save(plan);

        // when
        user.addSubscribed(Subscribed.of(plan));
        userRepository.save(user);

        // then: Neo4j에 저장된 유저를 조회해서 SUBSCRIBED 관계로 plan이 연결되어 있는지 확인
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getSubscribedHistory()).isNotNull();
        assertThat(savedUser.getSubscribedHistory()).hasSize(1);
        assertThat(savedUser.getSubscribedHistory().get(0).getPlan().getId())
                .isEqualTo(plan.getId());
    }
}
