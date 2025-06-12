package com.ixi_U.user.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.response.ShowReviewResponse;
import com.ixi_U.user.entity.Reviewed;
import com.ixi_U.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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
            .withAdminPassword("1q2w3e4r");
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
            assertThat(loadedUser).isPresent();
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

    @Nested
    @DisplayName("리뷰 리스트를 조회할 때")
    class Describe_showReview {

        private final int totalReviewCount = 6;
        private final int pageSize = 5;
        private Plan savedPlan;

        @BeforeEach
        void setUp() {

            savedPlan = planRepository.save(Plan.of("요금제 A"));

            for (int i = 0; i < totalReviewCount; i++) {
                User user = userRepository.save(
                        User.of("user" + i, "user" + i + "@mail.com", "kakao"));
                Reviewed reviewed = Reviewed.of(5 - i % 5, savedPlan, "user" + i + "의 리뷰입니다");
                user.addReviewed(reviewed);
                userRepository.save(user);
            }
        }

        @Test
        @DisplayName("첫 페이지 조회 시, 페이지 크기만큼 조회되며 다음 페이지가 존재한다")
        void it_returns_first_page_with_has_next_true() {
            // when
            Slice<ShowReviewResponse> page = reviewedRepository.findReviewedByPlanWithPaging(
                    savedPlan.getId(), PageRequest.of(0, pageSize));

            // then
            assertThat(page.getContent()).hasSize(pageSize);
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("마지막 페이지 조회 시, 남은 리뷰 수만큼 조회되며 다음 페이지가 존재하지 않는다")
        void it_returns_last_page_with_has_next_false() {

            int expectedLastPageSize = totalReviewCount % pageSize;

            // when
            Slice<ShowReviewResponse> page = reviewedRepository.findReviewedByPlanWithPaging(
                    savedPlan.getId(), PageRequest.of(1, pageSize));

            // then
            assertThat(page.getContent()).hasSize(expectedLastPageSize);
            assertThat(page.hasNext()).isFalse();
        }

        @Test
        @DisplayName("점수 기반 내림 차순 정렬이 잘 된다.")
        void it_returns_with_correct_point_order_desc() {
            // when
            Slice<ShowReviewResponse> page = reviewedRepository.findReviewedByPlanWithPaging(
                    savedPlan.getId(),
                    PageRequest.of(0, pageSize, Sort.by(Sort.DEFAULT_DIRECTION.DESC, "point")));

            List<ShowReviewResponse> content = page.getContent();

            // then
            for (int i = 1; i < content.size(); i++) {
                assertThat(content.get(i - 1).point())
                        .isGreaterThanOrEqualTo(content.get(i).point());
            }
        }

        @Test
        @DisplayName("생성 시간 기반 내림 차순 정렬이 잘 된다.")
        void it_returns_with_correct_createdAt_order_desc() {
            // when
            Slice<ShowReviewResponse> page = reviewedRepository.findReviewedByPlanWithPaging(
                    savedPlan.getId(),
                    PageRequest.of(0, pageSize, Sort.by(Sort.DEFAULT_DIRECTION.DESC, "createdAt")));

            List<ShowReviewResponse> content = page.getContent();

            // then
            for (int i = 1; i < content.size(); i++) {
                assertThat(content.get(i - 1).createdAt())
                        .isAfterOrEqualTo(content.get(i).createdAt());
            }
        }

        @Test
        @DisplayName("점수 기반 오름 차순 정렬이 잘 된다.")
        void it_returns_with_correct_score_order_asc() {
            // when
            Slice<ShowReviewResponse> page = reviewedRepository.findReviewedByPlanWithPaging(
                    savedPlan.getId(),
                    PageRequest.of(0, pageSize, Sort.by(Sort.DEFAULT_DIRECTION.ASC, "point")));

            List<ShowReviewResponse> content = page.getContent();

            // then
            for (int i = 1; i < content.size(); i++) {
                assertThat(content.get(i - 1).point())
                        .isLessThanOrEqualTo(content.get(i).point());
            }
        }

    }
}