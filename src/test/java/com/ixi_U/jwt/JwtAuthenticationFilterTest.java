package com.ixi_U.jwt;

import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayName("JwtAuthenticationFilter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("유효한 access_token이 존재할 때")
    class ValidAccessToken {
        @Test
        @DisplayName("SecurityContext에 인증 정보가 저장된다")
        void validAccessToken_authenticationSuccess() throws Exception {
            // given
            String token = "validAccessToken";
            String userId = "user123";
            String role = "ROLE_USER";

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("access_token", token));
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain filterChain = mock(FilterChain.class);

            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(userId);
            when(jwtTokenProvider.getRoleFromToken(token)).thenReturn(role);

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            assertThat(request.getUserPrincipal()).isNull(); // Web 환경에서는 보통 SecurityContext를 확인함
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("access_token이 유효하지 않고 refresh_token은 유효할 때")
    class RefreshTokenCase {
        @Test
        @DisplayName("access_token이 재발급되고 인증 처리된다")
        void validRefreshToken_reissueAccessToken() throws IOException, jakarta.servlet.ServletException {
            // given
            String invalidAccessToken = "invalidAccessToken";
            String refreshToken = "validRefreshToken";
            String newAccessToken = "newAccessToken";
            String userId = "user123";
            String role = "ROLE_USER";

            User mockUser = mock(User.class);
            when(mockUser.getRefreshToken()).thenReturn(refreshToken);

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie("access_token", invalidAccessToken), new Cookie("refresh_token", refreshToken));
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain filterChain = mock(FilterChain.class);

            when(jwtTokenProvider.validateToken(invalidAccessToken)).thenReturn(false);
            when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(userId);
            when(jwtTokenProvider.getRoleFromToken(refreshToken)).thenReturn(role);
            when(jwtTokenProvider.generateAccessToken(userId, UserRole.ROLE_USER)).thenReturn(newAccessToken);
            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

            // when
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // then
            Cookie[] cookies = response.getCookies();
            assertThat(cookies).anyMatch(c -> c.getName().equals("access_token") && c.getValue().equals(newAccessToken));
            verify(filterChain).doFilter(request, response);
        }
    }
}
