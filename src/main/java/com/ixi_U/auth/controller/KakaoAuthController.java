package com.ixi_U.auth.controller;

import com.ixi_U.auth.dto.KakaoUserResponse;
import com.ixi_U.auth.service.KakaoAuthService;
import com.ixi_U.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    private static final String FRONT_REDIRECT_BASE = "http://localhost:3000/login/status";

    @GetMapping("/login/auth/code/kakao")
    public RedirectView kakaoLogin(@RequestParam("code") String code){

        try {
            String kakaoAccessToken = kakaoAuthService.getAccessToken(code); // 카카오 access_token 요청
            KakaoUserResponse kakaoUser = kakaoAuthService.getUserInfoFromKakao(kakaoAccessToken); // 사용자 정보

            boolean isNewUser = kakaoAuthService.handleUserLoginFlow(kakaoUser);

            return redirectToStatusWithNewUserFlag(isNewUser);

        } catch (GeneralException e) {
            return redirectToStatusWithError(e.getExceptionName());
        }
    }

    private RedirectView redirectToStatusWithNewUserFlag(boolean isNewUser) {
        if (isNewUser) {
            return new RedirectView(FRONT_REDIRECT_BASE + "?isNewUser=" + "true"); // 신규 회원
        } else {
            return new RedirectView(FRONT_REDIRECT_BASE + "?isNewUser=" + "false"); // 기존 유저
        }

    }

    private RedirectView redirectToStatusWithError(String error) {
        return new RedirectView(FRONT_REDIRECT_BASE + "?error=" + error);
    }
}
