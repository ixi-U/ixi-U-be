package com.ixi_U.jwt;

import com.ixi_U.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private final String secret = "my-secret-key-which-is-very-long-for-hmac-algorithm-12345678901234567890123456789012";
    private final long accessTokenExp = 1000 * 60 * 60; // 1 hour
    private final int refreshTokenExp = 1000 * 60 * 60 * 24; // 1 day

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "SECRET_KEY", secret);
        ReflectionTestUtils.setField(jwtTokenProvider, "ACCESS_TOKEN_EXP", accessTokenExp);
        ReflectionTestUtils.setField(jwtTokenProvider, "REFRESH_TOKEN_EXP", refreshTokenExp);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("AccessToken 생성 시 사용자 ID와 권한이 포함되어야 한다")
    void generateAccessToken_shouldContainUserIdAndRole() {
        String token = jwtTokenProvider.generateToken("user123", UserRole.ROLE_USER);

        assertNotNull(token);
        assertEquals("user123", jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("ROLE_USER", jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("RefreshToken 생성 시 사용자 ID가 포함되어야 한다")
    void generateRefreshToken_shouldContainUserId() {
        String token = jwtTokenProvider.generateToken("user123", UserRole.ROLE_USER);

        assertNotNull(token);
        assertEquals("user123", jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("유효한 JWT 토큰은 true를 반환해야 한다")
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtTokenProvider.generateToken("user123", UserRole.ROLE_USER);

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("잘못된 JWT 토큰은 false를 반환해야 한다")
    void validateToken_shouldReturnFalseForMalformedToken() {
        String token = "invalid.token.value";

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("AccessToken 만료 시간 설정 값을 반환해야 한다")
    void getAccessTokenExp_shouldReturnConfiguredExpiration() {
        assertEquals(accessTokenExp, jwtTokenProvider.getAccessTokenExp());
    }

    @Test
    @DisplayName("RefreshToken 만료 시간 설정 값을 반환해야 한다")
    void getRefreshTokenExp_shouldReturnConfiguredExpiration() {
        assertEquals(refreshTokenExp, jwtTokenProvider.getRefreshTokenExp());
    }
}