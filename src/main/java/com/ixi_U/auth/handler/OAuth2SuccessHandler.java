package com.ixi_U.auth.handler;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.jwt.JwtTokenProvider;
import com.ixi_U.user.entity.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 *  로그인 성공 후 사용자 정보를 가져와서 토큰 생성 + 리다이랙트 경로 설정
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // JSESSIONID 제거
        }

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String userId = customOAuth2User.getUserId();
        UserRole userRole = customOAuth2User.getUserRole();
        Boolean isNewUser = customOAuth2User.isNewUser();

        log.info("userId = {}", userId);
        log.info("userRole = {}", userRole);

//        String accessToken = jwtTokenProvider.generateAccessToken(userId, userRole);
//        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        String accessToken = jwtTokenProvider.generateToken(userId, userRole);
        String refreshToken = jwtTokenProvider.generateToken(userId, userRole);

        log.info("AccessToken = {}", accessToken);
        log.info("RefreshToken = {}", refreshToken);

        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) jwtTokenProvider.getAccessTokenExp());

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) jwtTokenProvider.getRefreshTokenExp());

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        String host = request.getHeader("host");
        String frontBaseUrl = (host != null && host.contains("localhost"))
                ? "http://localhost:3000"
                : "https://ixiu.site";

        String redirectUrl = isNewUser
                ? frontBaseUrl + "/onboarding"
                : frontBaseUrl + "/plans";

//        response.sendRedirect(redirectUrl);
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", redirectUrl);
    }
}