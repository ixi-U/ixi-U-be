package com.ixi_U.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ixi_U.common.AbstractNeo4jContainer;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.entity.Subscribed;
import com.ixi_U.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataNeo4jTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class SubscribedRepositoryTest extends AbstractNeo4jContainer {


    @Autowired
    UserRepository userRepository;
    @Autowired
    PlanRepository planRepository;
    @Autowired
    SubscribedRepository subscribedRepository;

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
                User user = User.of("jinu", "jinu@mail.com", "kakao");
                Plan savedPlan = planRepository.save(Plan.of("요금제 A"));

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
                User savedUser = userRepository.save(User.of("jinu", "jinu@mail.com", "kakao"));
                Plan savedPlan = planRepository.save(Plan.of("요금제 A"));

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