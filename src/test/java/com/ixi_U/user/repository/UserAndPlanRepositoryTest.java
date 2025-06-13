package com.ixi_U.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.common.exception.enums.PlanException;
import com.ixi_U.common.exception.enums.UserException;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.request.CreateSubscribedRequest;
import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.service.SubscribedService;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@Import(SubscribedService.class)
@DataNeo4jTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Testcontainers 기반 Neo4j DB와 실제 연동되는 구독관계 저장 테스트")
class UserAndPlanRepositoryTest {

    private static Neo4jContainer<?> neo4jContainer;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PlanRepository planRepository;
    @Autowired
    SubscribedService subscribedService;

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


    @Test
    @DisplayName("회원과 요금제 생성 후 실제 SUBSCRIBED 관계가 DB에 저장된다")
    void givenUserAndPlan_whenUpdateSubscribed_thenSubscribedRelationshipIsSaved() {
        // given
        User user = User.of("홍승민", "hong@example.com", "KAKAO");
        user = userRepository.save(user);

        Plan plan = planRepository.save(Plan.of("요금제 A", 20000, 300, 200, 100, 29000,
                PlanType.ONLINE, "주의사항", 400,
                0, 100, false, 5, "기타 없음", 5, List.of(), List.of()
        ));

        // when
        user.addSubscribed(Subscribed.of(plan));
        userRepository.save(user);

        // then: Neo4j에 저장된 유저를 조회해서 SUBSCRIBED 관계로 plan이 연결되어 있는지 확인
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getSubscribedHistory())
                .isNotNull()
                .hasSize(1)
                .extracting(subscribed -> subscribed.getPlan().getId())
                .containsExactly(plan.getId());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외가 발생한다")
    void givenNonExistentUser_whenFindById_thenThrowsException() {
        // given
        String nonExistentUserId = "not_exist_user_id";

        // when & then
        assertThatThrownBy(() -> userRepository.findById(nonExistentUserId)
                .orElseThrow(() -> new GeneralException(UserException.USER_NOT_FOUND)))
                .isInstanceOf(GeneralException.class)
                .hasMessage(UserException.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("요금제를 변경하면 구독 이력이 누적된다")
    void givenChangedSubscription_whenAddDifferentSubscription_thenHistoryAccumulates() {
        // given
        User user = userRepository.save(User.of("홍길동", "hong@example.com", "KAKAO"));
        Plan planA = planRepository.save(Plan.of("요금제 A", 20000, 300, 200, 100, 29000,
                PlanType.ONLINE, "주의사항", 400,
                0, 100, false, 5, "기타 없음", 5, List.of(), List.of()
        ));
        Plan planB = planRepository.save(Plan.of("요금제 B", 20000, 300, 200, 100, 29000,
                PlanType.ONLINE, "주의사항", 400,
                0, 100, false, 5, "기타 없음", 5, List.of(), List.of()
        ));

        // 최초 구독 (A)
        user.addSubscribed(Subscribed.of(planA));
        userRepository.save(user);

        // B로 변경
        user.addSubscribed(Subscribed.of(planB));
        userRepository.save(user);

        // 다시 A로 변경 (이력 누적)
        user.addSubscribed(Subscribed.of(planA));
        userRepository.save(user);

        // then
        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getSubscribedHistory())
                .hasSize(3)
                .extracting(subscribed -> subscribed.getPlan().getId())
                .containsExactly(planA.getId(), planB.getId(), planA.getId());

    }


    @Test
    @DisplayName("관계 매핑/저장 로직 확인")
    void givenSubscribed_whenReloadedFromDb_thenSubscribedHistoryIsNotEmpty() {
        // 1. given
        User user = userRepository.save(User.of("홍길동", "hong@example.com", "KAKAO"));
        Plan plan = planRepository.save(Plan.of("요금제 A", 20000, 300, 200, 100, 29000,
                PlanType.ONLINE, "주의사항", 400,
                0, 100, false, 5, "기타 없음", 5, List.of(), List.of()
        ));

        // 2. when 서비스로 구독 추가
        subscribedService.updateSubscribed(user.getId(), new CreateSubscribedRequest(plan.getId()));

        // 3. DB에서 fresh 조회
        User freshUser = userRepository.findById(user.getId()).orElseThrow();

        // 4. then 이력이 1 이상이어야 정상!
        assertThat(freshUser.getSubscribedHistory()).isNotEmpty();
    }


    @Test
    @DisplayName("이미 구독 중인 요금제를 중복 구독할 때의 동작을 확인한다")
    void givenExistingSubscription_whenAddDuplicateSubscription_thenHandlesProperly() {
        // given
        User user = userRepository.save(User.of("홍길동", "hong@example.com", "KAKAO"));
        Plan plan = planRepository.save(Plan.of("요금제 A", 20000, 300, 200, 100, 29000,
                PlanType.ONLINE, "주의사항", 400,
                0, 100, false, 5, "기타 없음", 5, List.of(), List.of()
        ));

        CreateSubscribedRequest request = new CreateSubscribedRequest(plan.getId());

        // 최초 구독
        subscribedService.updateSubscribed(user.getId(), request);

        // when & then: 같은 요금제 중복 구독 시 예외 발생
        assertThatThrownBy(() ->
                subscribedService.updateSubscribed(user.getId(),
                        new CreateSubscribedRequest(plan.getId()))
        )
                .isInstanceOf(GeneralException.class)
                .hasMessage(PlanException.ALREADY_SUBSCRIBED_PLAN.getMessage());

    }

}
