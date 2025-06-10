package com.ixi_U.user.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.entity.Reviewed;
import com.ixi_U.user.entity.User;
import java.util.Optional;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataNeo4jTest
@ActiveProfiles("test")
@Transactional
@Testcontainers
class ReviewedRepositoryTest {


    @Container
    private static final Neo4jContainer<?> neo4j = new Neo4jContainer<>(
            DockerImageName.parse("neo4j:5"))
            .withAdminPassword("1q2w3e4r")
            .withReuse(true);

    @Autowired
    ReviewedRepository reviewedRepository;
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


    @Nested
    @DisplayName("리뷰를 저장할 때")
    class Describe_addReviewed {

        @Test
        @DisplayName("정상적으로 리뷰가 저장된다")
        void it_saves_review() {
            // given
            User savedUser = userRepository.save(User.of("jinu", "jinu@mail.com", "kakao"));
            Plan savedPlan = planRepository.save(Plan.of("요금제 A"));
            Reviewed reviewed = Reviewed.of(5, savedPlan, "안녕하세영");

            savedUser.addReviewed(reviewed);
            User finalUser = userRepository.save(savedUser);

            // when
            Optional<User> loadedUser = userRepository.findById(finalUser.getId());

            // then
            assertThat(loadedUser).isNotNull();
            assertThat(loadedUser.get().getReviewedHistory()).hasSize(1);
            assertThat(loadedUser.get().getReviewedHistory().get(0).getComment()).isEqualTo(
                    "안녕하세영");
        }
    }

    @Nested
    @DisplayName("리뷰 관계가 존재하는지 확인할 때")
    class Describe_existsReviewedRelation {

        @Nested
        @DisplayName("리뷰 관계가 존재하면")
        class Context_review_exists {

            @Test
            @DisplayName("true를 반환한다")
            void it_returns_true() {
                // given
                User savedUser = userRepository.save(User.of("jinu", "jinu@mail.com", "kakao"));
                Plan savedPlan = planRepository.save(Plan.of("요금제 A"));
                Reviewed reviewed = Reviewed.of(5, savedPlan, "안녕하세영");

                savedUser.addReviewed(reviewed);
                userRepository.save(savedUser);

                // when
                boolean existReview = reviewedRepository.existsReviewedRelation(savedUser.getId(),
                        savedPlan.getId());

                // then
                assertThat(existReview).isTrue();
            }
        }

        @Nested
        @DisplayName("리뷰 관계가 존재하지 않으면")
        class Context_review_not_exists {

            @Test
            @DisplayName("false를 반환한다")
            void it_returns_false() {
                // given
                User savedUser = userRepository.save(User.of("jinu", "jinu@mail.com", "kakao"));
                Plan savedPlan = planRepository.save(Plan.of("요금제 A"));

                // when
                boolean existReview = reviewedRepository.existsReviewedRelation(savedUser.getId(),
                        savedPlan.getId());

                // then
                assertThat(existReview).isFalse();
            }
        }
    }
}