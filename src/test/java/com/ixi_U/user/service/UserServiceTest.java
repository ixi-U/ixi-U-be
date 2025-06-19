package com.ixi_U.user.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.auth.service.CustomOAuth2UserService;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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

}
