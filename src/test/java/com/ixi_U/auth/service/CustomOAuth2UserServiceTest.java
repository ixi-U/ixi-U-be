package com.ixi_U.auth.service;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
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

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        oAuth2UserRequest = mock(OAuth2UserRequest.class);
        customOAuth2UserService = spy(new CustomOAuth2UserService(userRepository));
    }

    @Nested
    @DisplayName("loadUser()에서")
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

        @Test
        @DisplayName("기존 카카오 ID가 없으면 신규 유저로 저장하고 isNewUser는 true여야 한다")
        void givenNewKakaoUser_whenProcessOAuth2User_thenSaveUserAndReturnNewUser() {
            // given
            String kakaoId = "22222";
            String userId = "user-222";
            String nickname = "신규유저";

            Map<String, Object> profile = Map.of("nickname", nickname);
            Map<String, Object> kakaoAccount = new HashMap<>();
            kakaoAccount.put("email", null);
            kakaoAccount.put("profile", profile);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("id", kakaoId);
            attributes.put("kakao_account", kakaoAccount);

            OAuth2User fakeOAuth2User = new DefaultOAuth2User(List.of(), attributes, "id");

            when(userRepository.findByKakaoId(Long.valueOf(kakaoId))).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                return savedUser; // 실제 저장된 객체 그대로 반환
            });

            // when
            CustomOAuth2User result = (CustomOAuth2User) customOAuth2UserService.processOAuth2User(fakeOAuth2User);

            // then
            assertThat(result.isNewUser()).isTrue();
            assertThat(result.getName()).isEqualTo(nickname);
            assertThat(result.getUserRole()).isEqualTo(UserRole.ROLE_USER);
            verify(userRepository).save(any(User.class));
        }
    }
}