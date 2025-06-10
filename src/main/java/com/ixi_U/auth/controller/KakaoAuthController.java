package com.ixi_U.auth.controller;

import com.ixi_U.auth.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/login/auth/code/kakao")
    public RedirectView kakaoLogin(@RequestParam("code") String code){
        kakaoAuthService.kakaoAuthTokenResponse(code);
        return new RedirectView("http://localhost:3000/login/status?success=true");
        // 로그인성공하면 성공여부 체크하는 프론트화면으로 리다이렉트
    }
}
