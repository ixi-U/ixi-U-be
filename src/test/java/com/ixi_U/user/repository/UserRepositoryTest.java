package com.ixi_U.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.response.ShowReviewStatsResponse;
import com.ixi_U.user.entity.Reviewed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
@Testcontainers
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {

    @Container
    private static final Neo4jContainer<?> neo4j = new Neo4jContainer<>(
            DockerImageName.parse("neo4j:5.24"))
            .withAdminPassword("1q2w3e4r");
    @Autowired
    PlanRepository planRepository;
    @Autowired
    UserRepository userRepository;

    @DynamicPropertySource
    static void overrideNeo4jProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "1q2w3e4r");
    }

    @Nested
    @DisplayName("리뷰 평균 별점과 리뷰 개수를 구할 때")
    class Describe_check_review_rating_and_reviewCount {

        private final int totalReviewCount = 6;

        private double roundedAverage = 0;

        private Plan savedPlan;

        @BeforeEach
        void setUp() {

            savedPlan = planRepository.save(Plan.of("요금제 A", 20000, 300, 200, 100, 29000,
                    PlanType.ONLINE, "주의사항", 400,
                    0, 100, false, 5.0, "기타 없음", 5, List.of(), List.of()
            ));

            int sumOfReviewPoint = 0;
            // 저장된 리뷰 점수는 : 5,4,3,2,1,5 => 20 / 6 => 3.33 => 소숫점 첫 째 자리 반올림 3.3
            for (int i = 0; i < totalReviewCount; i++) {
                User user = userRepository.save(
                        User.of("user" + i, "user" + i + "@mail.com", "kakao", 123L, UserRole.ROLE_USER));

                int reviewPoint = 5 - i % 5;
                sumOfReviewPoint += reviewPoint;
                Reviewed reviewed = Reviewed.of(reviewPoint, savedPlan, "user" + i + "의 리뷰입니다");
                user.addReviewed(reviewed);
                userRepository.save(user);
            }

            double averageReviewRating = (double) sumOfReviewPoint / totalReviewCount;
            roundedAverage = Math.round(averageReviewRating * 10.0) / 10.0;
        }

        @Test
        @DisplayName("리뷰 개수는 totalReviewCount 이다")
        void it_return_same_with_total_review_count() {

            //when
            ShowReviewStatsResponse averagePointAndReviewCount = userRepository.findAveragePointAndReviewCount(
                    savedPlan.getId());

            //then
            assertThat(averagePointAndReviewCount.totalCount()).isEqualTo(totalReviewCount);
            assertThat(averagePointAndReviewCount.averagePoint()).isEqualTo(roundedAverage);
        }

    }

}