package com.ixi_U.plan.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.entity.PlanSortOption;
import com.ixi_U.plan.entity.PlanType;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataNeo4jTest
@Testcontainers
class PlanRepositoryTest {

    private static Neo4jContainer<?> neo4jContainer;

    @Autowired
    PlanRepository planRepository;

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
    class findPlansTest {

        @BeforeEach
        void setUp() {

            planRepository.deleteAll();

            // given
            Plan plan1 = Plan.of(
                    "요금제1", 20000, 300, 200, 100, 29000,
                    PlanType.ONLINE, "주의사항", 400,
                    0, 100, false, 5, "기타 없음", 5, List.of(), List.of()
            );
            Plan plan2 = Plan.of(
                    "요금제2", 10000, 300, 200, 100, 79000,
                    PlanType.ONLINE, "주의사항", 400,
                    0, 100, false, 5, "기타 없음", 1, List.of(), List.of()
            );
            Plan plan3 = Plan.of(
                    "요금제3", 40000, 300, 200, 100, 109000,
                    PlanType.ONLINE, "주의사항", 400,
                    0, 100, false, 5, "기타 없음", 3, List.of(), List.of()
            );

            planRepository.saveAll(List.of(plan1, plan2, plan3));
        }

        @Test
        @DisplayName("인기순 조건으로 요금제를 조회할 수 있다")
        void searchByPriority() {

            // when, then
            assertThat(
                    planRepository.findPlans(PageRequest.ofSize(3), PlanType.ONLINE,
                                    PlanSortOption.PRIORITY, null, null, null)
                            .getContent()
                            .stream()
                            .map(PlanSummaryDto::name)
                            .toList()
            ).containsExactly("요금제1", "요금제3", "요금제2");
        }

        @Test
        @DisplayName("가격 오름차순 조건으로 요금제를 조회할 수 있다")
        void searchByPriceAsc() {

            // when, then
            assertThat(
                    planRepository.findPlans(PageRequest.ofSize(3), PlanType.ONLINE,
                                    PlanSortOption.PRICE_ASC, null, null, null)
                            .getContent()
                            .stream()
                            .map(PlanSummaryDto::name)
                            .toList()
            ).containsExactly("요금제1", "요금제2", "요금제3");
        }

        @Test
        @DisplayName("가격 내림차순 조건으로 요금제를 조회할 수 있다")
        void searchByPriceDesc() {

            // when, then
            assertThat(
                    planRepository.findPlans(PageRequest.ofSize(3), PlanType.ONLINE,
                                    PlanSortOption.PRICE_DESC, null, null, null)
                            .getContent()
                            .stream()
                            .map(PlanSummaryDto::name)
                            .toList()
            ).containsExactly("요금제3", "요금제2", "요금제1");
        }

        @Test
        @DisplayName("데이터 내림차순 조건으로 요금제를 조회할 수 있다")
        void searchByDataDesc() {

            // when, then
            assertThat(
                    planRepository.findPlans(PageRequest.ofSize(3), PlanType.ONLINE,
                                    PlanSortOption.DATA_DESC, null, null, null)
                            .getContent()
                            .stream()
                            .map(PlanSummaryDto::name)
                            .toList()
            ).containsExactly("요금제3", "요금제1", "요금제2");
        }

        @DisplayName("검색으로 요금제를 조회할 수 있다")
        @ParameterizedTest
        @ValueSource(strings = {"요금제1", "요금제2", "요금제3"})
        void searchByKeyword(String keyword) {

            // when, then
            assertThat(
                    planRepository.findPlans(PageRequest.ofSize(3), PlanType.ONLINE,
                                    PlanSortOption.DATA_DESC, keyword, null, null)
                            .getContent()
                            .stream()
                            .map(PlanSummaryDto::name)
                            .toList()
            ).containsExactly(keyword);
        }
    }

}