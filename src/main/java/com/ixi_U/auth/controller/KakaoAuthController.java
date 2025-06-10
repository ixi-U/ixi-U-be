package com.ixi_U.auth.controller;

import com.ixi_U.auth.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/login/auth/code/kakao")
    public ResponseEntity<String> kakaoLogin(@RequestParam("code") String code){
        String result = kakaoAuthService.kakaoAuthTokenResponse(code);
        return ResponseEntity.ok(result);
    }
}
