package com.ixi_U.user.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.auth.service.CustomOAuth2UserService;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.user.dto.response.ShowMyInfoResponse;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.plan.repository.PlanRepository;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscribedRepository subscribedRepository;

    @Mock
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("카카오 소셜 회원 탈퇴 시 회원과 구독 정보 삭제 확인")
    void deleteKakaoUserAndSubscriptions() {
        // given
        String userId = "kakao123";
        when(userRepository.existsById(userId)).thenReturn(true);

        // when
        userService.deleteUserById(userId, response);

        // then: 쿠키 만료 호출 검증(optional)
        verify(customOAuth2UserService).expireCookie("access_token", response);
        verify(customOAuth2UserService).expireCookie("refresh_token", response);

        // DB 삭제 로직 검증
        verify(subscribedRepository).deleteAllByUserId(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("@AuthenticationPrincipal UserId를 통한 회원 탈퇴 로직 확인")
    void deleteUser_withCustomOAuth2User() {
        // given
        User dummyUser = User.of("nickname", "email@example.com", "kakao", 123456L,
                UserRole.ROLE_USER);
        when(userRepository.existsById(dummyUser.getId())).thenReturn(true);

        // SecurityContext에 인증 정보 설정
        CustomOAuth2User customUser = new CustomOAuth2User(
                dummyUser.getName(),
                dummyUser.getId(),
                dummyUser.getUserRole(),
                false
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(dummyUser.getId(), null,
                        customUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        userService.deleteUserById(customUser.getUserId(), response);

        // then
        verify(customOAuth2UserService).expireCookie("access_token", response);
        verify(customOAuth2UserService).expireCookie("refresh_token", response);
        verify(userRepository).deleteById(dummyUser.getId());
    }

    @Test
    @DisplayName("유저 정보 조회 - 정상 케이스")
    void findMyInfoByUserId_returnsUserInfo() {
        // given
        String userId = "user123";
        LocalDateTime createdAt = LocalDateTime.of(2024, 6, 20, 12, 0);

        User user = User.of("nickname", "user@example.com", "kakao", 123456L, UserRole.ROLE_USER);
        ReflectionTestUtils.setField(user, "createdAt", createdAt);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        ShowMyInfoResponse result = userService.findMyInfoByUserId(userId);

        // then
        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.name()).isEqualTo(user.getName());
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.userRole()).isEqualTo(user.getUserRole());
        assertThat(result.createdAt()).isEqualTo(createdAt.toLocalDate());
    }

    @DisplayName("리프레시 토큰 제거 - 정상 케이스")
    @Test
    void removeRefreshToken_success() {
        // given
        String userId = "user123";
        User user = User.of("nickname", "user@example.com", "kakao", 123456L, UserRole.ROLE_USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userService.removeRefreshToken(userId);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("온보딩 정보 업데이트 - 이메일만 수정")
    void updateOnboardingInfo_onlyEmailUpdated() {
        // given
        String userId = "user123";
        String email = "new@email.com";
        String planId = null;

        User user = User.of("nickname", "old@email.com", "kakao", 123456L, UserRole.ROLE_USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userService.updateOnboardingInfo(userId, email, planId);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(captor.capture());

        assertThat(captor.getAllValues().get(0).getEmail()).isEqualTo("new@email.com");
        assertThat(captor.getAllValues().get(1).getEmail()).isEqualTo("new@email.com");
    }

    @Test
    @DisplayName("온보딩 정보 업데이트 - 이메일과 요금제 함께 수정")
    void updateOnboardingInfo_withPlanSelection() {
        // given
        String userId = "user123";
        String email = "new@email.com";
        String planId = "plan123";

        User user = User.of("nickname", "old@email.com", "kakao", 123456L, UserRole.ROLE_USER);
        Plan plan = Plan.of(
                "요금제A", 1000, 500, 100, 200, 30000, PlanType.FIVE_G_LTE,
                "데이터 소진 시 속도제한", 1000, 18, 60, false, 1,
                "기타정보", 1,
                new ArrayList<>(), new ArrayList<>()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        userService.updateOnboardingInfo(userId, email, planId);

        // then
        verify(userRepository, times(2)).save(any(User.class));
        verify(planRepository).findById(planId);
        // Optional: 구독이 정상적으로 추가되었는지는 subscribedRepository.save() 등을 통해 검증
    }

}
