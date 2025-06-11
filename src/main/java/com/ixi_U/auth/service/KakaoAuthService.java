package com.ixi_U.auth.service;

import com.ixi_U.auth.dto.KakaoUserResponse;
import com.ixi_U.auth.exception.KakaoAuthException;
import com.ixi_U.auth.dto.KakaoTokenResponse;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    @Value("${kakao.client_id}")
    private String restApiKey;

    @Value("${kakao.redirect}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    private final UserRepository userRepository;


    // 1. 인가 코드로 accessToken 요청
    public String getAccessToken(String code) {

        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 바디
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", restApiKey);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
//        params.add("client_secret", restApiKey);

        // 요청 객체 만들기
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // Post 요청 전송 - json 형식으로 응답이 오면 dto에 매핑
//        try {
//            ResponseEntity<KakaoTokenResponse> response =
//                    restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponse.class);
//            return response.getBody().getAccess_token();
//        } catch (HttpClientErrorException e) {
//            log.error("카카오 access token 발급 실패: {}", e.getResponseBodyAsString());
//            throw new GeneralException(KakaoAuthException.TOKEN_ISSUE_FAILED);
//        }

        try {
            ResponseEntity<KakaoTokenResponse> response =
                    restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponse.class);

            KakaoTokenResponse tokenResponse = response.getBody();

            if (tokenResponse == null || tokenResponse.access_token() == null) {
                log.error("카카오 토큰 응답이 null이거나 access token이 없습니다.");
                throw new GeneralException(KakaoAuthException.TOKEN_ISSUE_FAILED);
            }

            return tokenResponse.access_token();
        } catch (HttpClientErrorException e) {
            log.error("카카오 access token 발급 실패: {}", e.getResponseBodyAsString());
            throw new GeneralException(KakaoAuthException.TOKEN_ISSUE_FAILED);
        }

    } // getAccessToken


    // 2. kakao access_token을 기반으로 사용자 정보 조회
    public KakaoUserResponse getUserInfoFromKakao(String accessToken) {

        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 카카오로부터 사용자 정보 받음
        ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, entity, KakaoUserResponse.class
        );

//        if (!response.getStatusCode().is2xxSuccessful()) {
//            throw new KakaoAuthException("Failed to get user info");
//        }
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new GeneralException(KakaoAuthException.USER_INFO_REQUEST_FAILED);
        }
        return response.getBody();
    }

    public boolean handleUserLoginFlow(KakaoUserResponse kakaoUser) {

        Long kakaoId = kakaoUser.id();

        Optional<User> optionalUser = userRepository.findByKakaoId(kakaoId);

        if (optionalUser.isPresent()) {
            return false; //  기존 유저
        }

        // 신규 유저
        String nickname = kakaoUser.getNickname();
        String email =  "temp_kakao_" + kakaoId + "@example.com";

        // 신규 유저 DB 저장
        User newUser = User.of(nickname, email, "kakao", kakaoId);
        userRepository.save(newUser);

        return true;
    }
}
