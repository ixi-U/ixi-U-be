package com.ixi_U.auth.controller;

import com.ixi_U.auth.dto.TokenPair;
import com.ixi_U.auth.service.KakaoAuthService;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${front.login-redirect-url}")
    private String loginRedirectUrl;

    @GetMapping("/login/auth/code/kakao")
    public ResponseEntity<Void> kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) {
        try {
            TokenPair tokens = kakaoAuthService.loginAndIssueTokens(code); // 사용자 정보를 바탕으로 jwt 발급

            response.addCookie(createCookie("access_token", tokens.accessToken(), (int) jwtTokenProvider.getAccessTokenExp()));
            response.addCookie(createCookie("refresh_token", tokens.refreshToken(), (int) jwtTokenProvider.getRefreshTokenExp()));

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", loginRedirectUrl)
                    .build();

        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", loginRedirectUrl + "?error=" + e.getExceptionName())
                    .build();
        }
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
