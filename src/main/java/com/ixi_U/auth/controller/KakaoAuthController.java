package com.ixi_U.auth.controller;

import com.ixi_U.auth.service.KakaoAuthService;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/login/auth/code/kakao")
    public ResponseEntity<Void> kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) {
        try {
            String jwt = kakaoAuthService.loginAndIssueJwt(code); // 사용자 정보를 바탕으로 jwt 발급
//            String jwt = jwtTokenProvider.generateToken("1", "ROLE_USER");

            // 쿠키에 Jwt를 담아서 프론트에 전달
            Cookie cookie = new Cookie("access_token", jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24); // 1일

            response.addCookie(cookie);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:3000/login/status")
                    .build();

        } catch (GeneralException e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:3000/login/status?error=" + e.getExceptionName())
                    .build();
        }
    }
}