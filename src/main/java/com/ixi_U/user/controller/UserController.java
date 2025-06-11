package com.ixi_U.user.controller;

import com.ixi_U.auth.service.KakaoAuthService;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
public class UserController {
//
//    private final KakaoAuthService kakaoAuthService; // 카카오 API와 통신하여 사용자 정보 가져오기
//    private final UserService userService; // 로그인한 사용자가 신규 사용자인지 기존 사용자인지 확인
//    private final JwtProvider jwtProvider; // 로그인 성공 시 jwt 토큰 생성
//
//    @PostMapping("/kakao")
//    public ResponseEntity<LoginResponse> kakaoLogin(@RequestBody KakaoLoginRequest request) {
//
//        KakaoUserInfo userInfo = kakaoAuthService.requestUserInfo(request.getAccessToken());
//
//        User user = userService.findOrCreateUser(userInfo.getNickname(), "kakao");
//
//        String jwt = jwtProvider.createToken(user.getId());
//
//        boolean emailRequired = (user.getEmail() == null);
//
//        return ResponseEntity.ok(new LoginResponse(jwt, emailRequired));
//    }
}
