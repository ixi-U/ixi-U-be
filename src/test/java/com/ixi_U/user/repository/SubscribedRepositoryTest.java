package com.ixi_U.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataNeo4jTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class SubscribedRepositoryTest {


    private static Neo4jContainer<?> neo4jContainer;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PlanRepository planRepository;
    @Autowired
    SubscribedRepository subscribedRepository;

    @BeforeAll
    static void initializeNeo4j() {

        neo4jContainer = new Neo4jContainer<>(DockerImageName.parse("neo4j:5.24"))
                .withAdminPassword("haruharu");

        neo4jContainer.start();
    }

    @AfterAll
    static void stopNeo4j() {

        neo4jContainer.close();
    }

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }


    @Nested
    @DisplayName("구독 관계가 존재하는지 확인할 때")
    class Describe_existsSubscribeRelation {

        @Nested
        @DisplayName("구독 관계가 존재하면")
        class Context_subscribe_exists {

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                //given
                User user = User.of("jinu", "jinu@mail.com", "kakao", 123L, UserRole.ROLE_USER);
                Plan savedPlan = planRepository.save(Plan.of(
                        "요금제 A", 20000, 300, 200, 100, 29000,
                        PlanType.ONLINE, "주의사항", 400,
                        0, 100, false, 5.0, "기타 없음", 5, List.of(), List.of()
                ));

                user.addSubscribed(Subscribed.of(savedPlan));
                User savedUser = userRepository.save(user);

                //when
                boolean existsSubscribe = subscribedRepository.existsSubscribeRelation(
                        savedUser.getId(),
                        savedPlan.getId());

                //then
                assertThat(existsSubscribe).isTrue();
            }
        }

        @Nested
        @DisplayName("구독 관계가 존재하지 않으면")
        class Context_subscribe_not_exists {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                //given
                User savedUser = userRepository.save(User.of("jinu", "jinu@mail.com", "kakao", 123L,UserRole.ROLE_USER));
                Plan savedPlan = planRepository.save(Plan.of(
                        "요금제 A", 20000, 300, 200, 100, 29000,
                        PlanType.ONLINE, "주의사항", 400,
                        0, 100, false, 5.0, "기타 없음", 5, List.of(), List.of()
                ));

                //when
                boolean existsSubscribe = subscribedRepository.existsSubscribeRelation(
                        savedUser.getId(),
                        savedPlan.getId());

                //then
                assertThat(existsSubscribe).isFalse();
            }
        }
    }

}
