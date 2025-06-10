package com.ixi_U.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.common.exception.enums.PlanException;
import com.ixi_U.common.exception.enums.UserException;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.CreateSubscribedRequest;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@DisplayName("Mock을 사용한 SubscribedService 단위 테스트")
class SubscribedServiceTest {

    @InjectMocks
    SubscribedService subscribedService;

    @Mock
    UserRepository userRepository;

    @Mock
    PlanRepository planRepository;

    @Nested
    class RegisterSubscribed {

        @Test
        @DisplayName("정상 케이스: 유효한 userId/planId로 구독 등록 시 User에 관계가 추가되고 저장된다.")
        void givenValidUserIdAndPlanId_whenRegisterSubscribed_thenUserIsUpdated() {
            // given
            String userId = "user-1";
            String planId = "plan-1";
            User user = User.of("홍길동", "hong@example.com", "KAKAO");
            Plan plan = Plan.of("5G 요금제");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
            when(userRepository.save(any(User.class))).thenAnswer(
                    invocation -> invocation.getArgument(0));

            CreateSubscribedRequest request = new CreateSubscribedRequest(planId);

            // when
            subscribedService.updateSubscribed(userId, request);

            // then
            assertThat(user.getSubscribedHistory()).hasSize(1);
            assertThat(user.getSubscribedHistory().get(0).getPlan()).isEqualTo(plan);

            // 실제 저장 호출이 되었는지 확인
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("예외 케이스: 존재하지 않는 userId로 구독 등록 시 UserException이 발생한다.")
        void givenNonExistentUserId_whenRegisterSubscribed_thenThrowsUserException() {
            // given
            String userId = "not-exist";
            String planId = "plan-1";
            when(userRepository.findById(userId)).thenReturn(Optional.empty());
            CreateSubscribedRequest request = new CreateSubscribedRequest(planId);

            // when & then
            assertThatThrownBy(() -> subscribedService.updateSubscribed(userId, request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessageContaining(UserException.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예외 케이스: 존재하지 않는 planId로 구독 등록 시 PlanException이 발생한다.")
        void givenNonExistentPlanId_whenRegisterSubscribed_thenThrowsPlanException() {
            // given
            String userId = "user-1";
            String planId = "not-exist";
            User user = User.of("홍길동", "hong@example.com", "KAKAO");
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(planRepository.findById(planId)).thenReturn(Optional.empty());
            CreateSubscribedRequest request = new CreateSubscribedRequest(planId);

            // when & then
            assertThatThrownBy(() -> subscribedService.updateSubscribed(userId, request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessageContaining(PlanException.PLAN_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예외 케이스: planId가 null이면 GeneralException이 발생한다.")
        void givenNullPlanId_whenRegisterSubscribed_thenThrowsException() {
            // given
            String userId = "user-1";
            String planId = null;
            User user = User.of("홍길동", "hong@example.com", "KAKAO");
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            CreateSubscribedRequest request = new CreateSubscribedRequest(planId);

            // when & then
            assertThatThrownBy(() -> subscribedService.updateSubscribed(userId, request))
                    .isInstanceOf(GeneralException.class);
        }
    }
}
