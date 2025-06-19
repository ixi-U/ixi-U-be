package com.ixi_U.jwt;

import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import com.ixi_U.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

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
        return uri.startsWith("/oauth2") || uri.startsWith("/login") || uri.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("🔍 JwtAuthenticationFilter 진입: {}", request.getRequestURI());

        // 1. 쿠키에서 토큰 추출
        String token = extractAccessTokenFromCookie(request);
        log.info("🔑 추출된 토큰: {}", token);

        // 2. 쿠키 유효성 검사
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 3. 토큰에서 사용자 정보 파싱
            String userId = jwtTokenProvider.getUserIdFromToken(token).toString(); // subject는 문자열
            String role = jwtTokenProvider.getRoleFromToken(token);
            log.info("✅ 인증 성공 - userId: {}, role: {}", userId, role);

            // 4. 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority(role)));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 5. SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else {
            log.info("❌ 인증 실패 - 유효하지 않은 access token");

            // todo access token 이 없으면 -> 인증 실패 요청 차단 ?
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing token");
//            return; // 요청 차단

            String refreshToken = extractRefreshTokenFromCookie(request);

            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                String userId = jwtTokenProvider.getUserIdFromToken(refreshToken).toString();
                String role = jwtTokenProvider.getRoleFromToken(refreshToken);

                // refresh token이 DB에 저장된 값과 일치하는지 검증 필요
                User user = userRepository.findById(userId).orElse(null);

                if (user != null && refreshToken.equals(user.getRefreshToken())) {
                    // access token 재발급
                    String newAcceessToken = jwtTokenProvider.generateAccessToken(userId, role); // USER_ROLE

                    // 쿠키에 다시 저장
                    Cookie newAccessTokenCookie = new Cookie("access_token", newAcceessToken);
                    newAccessTokenCookie.setHttpOnly(true);
                    newAccessTokenCookie.setPath("/");
                    newAccessTokenCookie.setMaxAge((int) ACCESS_TOKEN_EXP);
                    response.addCookie(newAccessTokenCookie);

                    // 인증 처리
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority(role)));
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    log.info("♻️ access token 재발급 완료 - userId: {}", userId);
                }
            }
        }
        filterChain.doFilter(request, response);
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
