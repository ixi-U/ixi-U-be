package com.ixi_U.user.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ixi_U.security.oauth2.dto.CustomOAuth2User;
import com.ixi_U.security.oauth2.dto.OAuth2UserDto;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.SubscribedRepository;
import com.ixi_U.user.repository.UserRepository;
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

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("카카오 소셜 회원 탈퇴 시 회원과 구독 정보 삭제 확인")
    void deleteKakaoUserAndSubscriptions() {
        // given
        String userId = "kakao123";
        when(userRepository.existsById(userId)).thenReturn(true);

        // when
        userService.deleteUserById(userId);

        // then
        verify(subscribedRepository).deleteAllByUserId(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("@AuthenticationPrincipal UserId를 통한 회원 탈퇴 로직 확인")
    void deleteUser_withCustomOAuth2User() {
        // given
        User dummyUser = User.createSocialLoginUser("nickname", "email@example.com", "kakao_123456");
        when(userRepository.save(dummyUser)).thenReturn(dummyUser);
        when(userRepository.existsById(dummyUser.getId())).thenReturn(true);

        userRepository.save(dummyUser);

        CustomOAuth2User customUser = new CustomOAuth2User(
                new OAuth2UserDto("kakao_123", "nickname", "profile_url", "user@email.com"),
                dummyUser.getId(),
                dummyUser.getUserRole()
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(dummyUser.getId(), null,
                        customUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        userService.deleteUserById(customUser.getUserId());

        // then
        verify(userRepository).deleteById(dummyUser.getId());
    }

}
