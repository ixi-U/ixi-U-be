package com.ixi_U.auth.controller;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final CustomOAuth2User customOAuth2User;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // access_token 쿠키 제거
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setMaxAge(0); // 삭제
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);

        // refresh_token 쿠키 제거
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);

        // 쿠키를 응답에 추가
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

//         필요하다면 DB에 저장된 refreshToken도 삭제
        userService.deleteRefreshToken(customOAuth2User.getUserId());

        return ResponseEntity.ok("로그아웃 처리 완료");
    }
}

