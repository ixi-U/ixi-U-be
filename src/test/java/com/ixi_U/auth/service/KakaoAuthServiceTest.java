package com.ixi_U.auth.service;

import com.ixi_U.auth.dto.KakaoTokenResponse;
import com.ixi_U.auth.dto.KakaoUserResponse;
import com.ixi_U.auth.exception.KakaoAuthException;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.jwt.JwtTokenProvider;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KakaoAuthServiceTest {

    @InjectMocks
    private KakaoAuthService kakaoAuthService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kakaoAuthService = new KakaoAuthService(restTemplate, userRepository, jwtTokenProvider);
    }

    @Test
    void 신규사용자_DB_정상저장() {
        KakaoUserResponse.Profile profile = new KakaoUserResponse.Profile("userA");
        KakaoUserResponse.KakaoAccount kakaoAccount = new KakaoUserResponse.KakaoAccount(profile);

        KakaoUserResponse userResponse = new KakaoUserResponse(123L, kakaoAccount);
        when(userRepository.findByKakaoId(123L)).thenReturn(Optional.empty());

        boolean isNew = kakaoAuthService.handleUserLoginFlow(userResponse);

        assertThat(isNew).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 기존유저_존재시_false반환() {
        KakaoUserResponse.Profile profile = new KakaoUserResponse.Profile("test_name");
        KakaoUserResponse.KakaoAccount kakaoAccount = new KakaoUserResponse.KakaoAccount(profile);

        KakaoUserResponse userResponse = new KakaoUserResponse(123L, kakaoAccount);
        when(userRepository.findByKakaoId(123L)).thenReturn(Optional.of(mock(User.class)));

        boolean isNew = kakaoAuthService.handleUserLoginFlow(userResponse);

        assertThat(isNew).isFalse();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void 카카오_access_token으로_유저정보_정상_가져옴() {
        KakaoUserResponse.Profile profile = new KakaoUserResponse.Profile("test_name");
        KakaoUserResponse.KakaoAccount kakaoAccount = new KakaoUserResponse.KakaoAccount(profile);

        KakaoUserResponse mockResponse = new KakaoUserResponse(123L, kakaoAccount);
        ResponseEntity<KakaoUserResponse> entity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("https://kapi.kakao.com/v2/user/me"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoUserResponse.class)
        )).thenReturn(entity);

        KakaoUserResponse result = kakaoAuthService.getUserInfoFromKakao("access_token");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(123L);
        assertThat(result.getNickname()).isEqualTo("test_name");
    }
}
