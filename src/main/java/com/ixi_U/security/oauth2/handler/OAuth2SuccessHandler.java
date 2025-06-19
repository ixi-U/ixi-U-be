package com.ixi_U.security.oauth2.handler;

import com.ixi_U.security.jwt.provider.JwtProvider;
import com.ixi_U.security.jwt.provider.dto.JwtProviderRequestDto;
import com.ixi_U.security.jwt.provider.dto.JwtProviderResponseDto;
import com.ixi_U.security.jwt.service.JwtService;
import com.ixi_U.security.oauth2.dto.CustomOAuth2User;
import com.ixi_U.security.utils.CookieUtils;
import com.ixi_U.user.entity.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String userId = customOAuth2User.getUserId();
        UserRole userRole = customOAuth2User.getUserRole();

        // 1. 토큰 발급
        JwtProviderRequestDto jwtProviderRequestDto = JwtProviderRequestDto.of(userId, userRole);
        JwtProviderResponseDto jwtProviderResponseDto = jwtService.generateAccessTokenAndRefreshToken(jwtProviderRequestDto);

        log.info("Access Token = {}", jwtProviderResponseDto.getAccessToken());
        log.info("Refresh Token = {}", jwtProviderResponseDto.getRefreshToken());

        // 2. RefreshToken 저장, 이미 있다면 덮어씌우기
//        SaveRefreshTokenDto saveRefreshTokenDto = SaveRefreshTokenDto.of(userId, jwtProviderResponseDTO.getRefreshToken());
//        jwtService.saveRefreshTokenForOAuth2Login(saveRefreshTokenDto);

        // 3. response 에 Token 을 담은 Cookie 저장
        CookieUtils.setTokenCookie(jwtProviderResponseDto, response);

        // 4. redirect uri 지정 (임시)
//        response.sendRedirect("http://localhost:8080/login-success");
    }
}
