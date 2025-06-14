package com.ixi_U.auth.handler;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.jwt.JwtTokenProvider;
import com.ixi_U.user.entity.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User CustomOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String userId = CustomOAuth2User.getUserId();
        UserRole userRole = CustomOAuth2User.getUserRole();

        log.info("userId = {}", userId);
        log.info("userRole = {}", userRole);

        String accessToken = jwtTokenProvider.generateAccessToken(userId, userRole);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

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

        response.sendRedirect("http://localhost:3000/login/status");
    }
}