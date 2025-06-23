package com.ixi_U.jwt;

import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// jwt access token 에 대한 인가 확인 필터
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.access-token-expiration-time}")
    private long ACCESS_TOKEN_EXP;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 예외 URI 정의 (예: 로그인 관련 URI 등은 필터 패스)
        String uri = request.getRequestURI();
        return uri.startsWith("/oauth2") || uri.startsWith("/login") || uri.equals("/favicon.ico") || uri.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("🔍 JwtAuthenticationFilter 진입: {}", request.getRequestURI());

        try {
            String accessToken = extractAccessTokenFromCookie(request);

            // access tokend이 유효하면
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                // 엑세스에서 정보 추출
                String userId = jwtTokenProvider.getUserIdFromToken(accessToken).toString();
                String role = jwtTokenProvider.getRoleFromToken(accessToken);

                if (role == null) {
                    log.error("❌ JWT에서 role을 추출하지 못했습니다. token: {}", accessToken);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                log.info("✅ access token 인증 성공 - userId: {}, role: {}", userId, role);

                // security
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority(role)));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
                return;
            }

            log.info("❌ access token 인증 실패 - 재발급 시도");
            log.info("❗ refresh token 유효성 검사");
            String refreshToken = extractRefreshTokenFromCookie(request);

            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                log.info("✅ refresh 토큰 유효");
                String userId = jwtTokenProvider.getUserIdFromToken(refreshToken).toString();
                String roleString = jwtTokenProvider.getRoleFromToken(refreshToken);

//                if (roleString == null) {
//                    log.warn("❗ refresh token에서 role 정보 누락됨: {}", refreshToken);
//                    filterChain.doFilter(request,response);
//                    return;
//                }
                // 리프레시가 아직 유효하다면 리프레시로부터 access 재발급
                UserRole userRole = UserRole.from(roleString);
//                User user = userRepository.findById(userId).orElse(null);

//                if (user != null && refreshToken.equals(user.getRefreshToken())) {
                String newAccessToken = jwtTokenProvider.generateToken(userId, userRole);
                Cookie newAccessTokenCookie = new Cookie("access_token", newAccessToken);
                newAccessTokenCookie.setHttpOnly(true);
                newAccessTokenCookie.setPath("/");
                newAccessTokenCookie.setMaxAge((int) ACCESS_TOKEN_EXP);
                response.addCookie(newAccessTokenCookie);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority(roleString)));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("♻️ access token 재발급 완료 - userId: {}", userId);

                filterChain.doFilter(request, response);
                return;
//                } else {
//                    log.warn("❗ refresh token 유효하지만 DB 정보와 불일치 또는 사용자 없음");
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    return;
//                }
            }

            log.warn("❌ access + refresh token 모두 유효하지 않음 -> 로그인하지 않은 사용자");

            filterChain.doFilter(request, response); // 로그인하지 않은 사용자
        } catch (Exception e) {
            log.error("❌ JwtAuthenticationFilter 예외 발생", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    // access 토큰 추출
    private String extractAccessTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // refresh 토큰 추출
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("refresh_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

