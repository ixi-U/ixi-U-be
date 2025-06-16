package com.ixi_U.auth.controller;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.jwt.JwtTokenProvider;
import com.ixi_U.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = extractAccessTokenFromCookie(request);

        // 1. 쿠키에서 access_token을 꺼내 유효성 검사
        if (accessToken == null || !jwtTokenProvider.validateToken(accessToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        // 사용자 ID 추출
        String userId = jwtTokenProvider.getUserIdFromToken(accessToken).toString();

        // 해당 유저의 refreshToken 삭제
        userService.removeRefreshToken(userId);

        // 쿠키 제거
        expireCookie("access_token", response);
        expireCookie("refresh_token", response);

        return ResponseEntity.ok("로그아웃 완료");
    }

    private String extractAccessTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void expireCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
    }
}