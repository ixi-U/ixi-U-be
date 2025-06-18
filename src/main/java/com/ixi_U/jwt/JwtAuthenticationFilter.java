package com.ixi_U.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

// jwt access token 에 대한 인가 확인 필터
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // 필터 동작 안하도록
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//
//        return true;
//    }

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
        String token = extractTokenFromCookie(request);
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
            log.info("❌ 인증 실패 - 유효하지 않은 토큰");
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing token");
//            return; // 요청 차단
        }
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
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
}
