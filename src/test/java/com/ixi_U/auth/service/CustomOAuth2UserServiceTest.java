package com.ixi_U.auth.service;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayName("CustomOAuth2UserService 단위 테스트")
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        oAuth2UserRequest = mock(OAuth2UserRequest.class);
        customOAuth2UserService = spy(new CustomOAuth2UserService(userRepository));
    }

    @Nested
    @DisplayName("loadUser()는")
    class LoadUser {

        @Test
        @DisplayName("기존 카카오 ID가 존재하면 기존 유저 정보를 반환하고 isNewUser는 false여야 한다")
        void givenExistingKakaoId_whenLoadUser_thenReturnExistingUserAndIsNewFalse() {

            // given
            String userId = "f47ac10b-58cc-4372-a567-0e02b2c3d479"; // UUID
            String kakaoId = "11111"; // 카카오가 제공하는 고유 Pk
            String nickname = "기존유저";

            User existingUser = mock(User.class);

            when(existingUser.getId()).thenReturn(userId);
            when(existingUser.getName()).thenReturn(nickname);
            when(existingUser.getUserRole()).thenReturn(UserRole.ROLE_USER);

            OAuth2User oAuth2User = new CustomOAuth2User(nickname, userId, UserRole.ROLE_USER, false);

            doReturn(oAuth2User).when(customOAuth2UserService).loadUser(oAuth2UserRequest);
            when(userRepository.findByKakaoId(Long.valueOf(kakaoId))).thenReturn(Optional.of(existingUser));

            // when
            OAuth2User result = customOAuth2UserService.loadUser(oAuth2UserRequest);

            // then
            CustomOAuth2User customUser = (CustomOAuth2User) result;
            assertThat(customUser.isNewUser()).isFalse();
            assertThat(customUser.getUserId()).isEqualTo(userId);
            assertThat(customUser.getName()).isEqualTo(nickname);
        }
    }
}