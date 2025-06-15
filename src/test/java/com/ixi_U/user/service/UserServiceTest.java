package com.ixi_U.user.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ixi_U.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("카카오 소셜 유저 탈퇴 시 유저와 구독 정보 삭제 확인")
    void deleteKakaoUserAndSubscriptions() {
        // given
        String userId = "kakao123";
        when(userRepository.existsById(userId)).thenReturn(true);

        // when
        userService.deleteUserById(userId);

        // then
        verify(userRepository).deleteById(userId);
    }
}

