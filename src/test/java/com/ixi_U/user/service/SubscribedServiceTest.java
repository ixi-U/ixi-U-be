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
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.dto.request.CreateSubscribedRequest;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
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
        @DisplayName("유효한 userId/planId로 구독 등록 시 User에 관계가 추가되고 정상적으로 저장된다.")
        void givenValidUserIdAndPlanId_whenRegisterSubscribed_thenUserIsUpdated() {
            // given
            String userId = "user-1";
            String planId = "plan-1";
            User user = User.of("홍길동", "hong@example.com", "KAKAO", 123L, UserRole.ROLE_USER);
            Plan plan = Plan.of("요금제 A", 20000, 300, 200, 100, 29000,
                    PlanType.ONLINE, "주의사항", 400,
                    0, 100, false, 5.0, "기타 없음", 5, List.of(), List.of()
            );

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
        @DisplayName("존재하지 않는 userId로 구독 등록 시 UserException이 발생한다.")
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
        @DisplayName("존재하지 않는 planId로 구독 등록 시 PlanException이 발생한다.")
        void givenNonExistentPlanId_whenRegisterSubscribed_thenThrowsPlanException() {
            // given
            String userId = "user-1";
            String planId = "not-exist";
            User user = User.of("홍길동", "hong@example.com", "KAKAO", 123L,UserRole.ROLE_USER);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(planRepository.findById(planId)).thenReturn(Optional.empty());
            CreateSubscribedRequest request = new CreateSubscribedRequest(planId);

            // when & then
            assertThatThrownBy(() -> subscribedService.updateSubscribed(userId, request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessageContaining(PlanException.PLAN_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("planId가 null이면 GeneralException이 발생한다.")
        void givenNullPlanId_whenRegisterSubscribed_thenThrowsException() {
            // given
            String userId = "user-1";
            String planId = null;
            User user = User.of("홍길동", "hong@example.com", "KAKAO", 123L,UserRole.ROLE_USER);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            CreateSubscribedRequest request = new CreateSubscribedRequest(planId);

            // when & then
            assertThatThrownBy(() -> subscribedService.updateSubscribed(userId, request))
                    .isInstanceOf(GeneralException.class);
        }

        @Test
        @DisplayName("planId가 빈 문자열이면 GeneralException이 발생한다.")
        void givenEmptyPlanId_whenRegisterSubscribed_thenThrowsException() {
            // given
            String userId = "user-1";
            String planId = "";
            User user = User.of("홍길동", "hong@example.com", "KAKAO", 123L,UserRole.ROLE_USER);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            CreateSubscribedRequest request = new CreateSubscribedRequest(planId);

            // when & then
            assertThatThrownBy(() -> subscribedService.updateSubscribed(userId, request))
                    .isInstanceOf(GeneralException.class);
        }
    }
}
