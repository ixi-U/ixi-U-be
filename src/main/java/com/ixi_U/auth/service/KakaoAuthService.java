package com.ixi_U.auth.service;

import com.ixi_U.auth.exception.KakaoAuthException;
import com.ixi_U.auth.dto.KakaoTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    @Value("${kakao.client_id}")
    private String restApiKey;

    @Value("${kakao.redirect}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public String kakaoAuthTokenResponse(String code) {
        // 1. 인가 코드로 토큰 요청
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        // 요청 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // header 객체에 contenttype 지정
        // MediaType.APPLICATION_FORM_URLENCODED: application/x-www-form-urlencoded 형식을 의미
        // application/x-www-form-urlencoded는 다음과 같은 경우에 사용된다 :
        //HTML <form>에서 기본으로 사용하는 전송 방식
        //POST 요청 시, 데이터를 key-value 쌍으로 보내고 싶을 때

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
        ResponseEntity<KakaoTokenResponse> response =
                restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new KakaoAuthException("Failed to get token");
        }

//        log.info("토큰 - access_token : {}, expire_in : {} 초",
//                response.getBody().getAccess_token(), response.getBody().getExpires_in());

        return "ok";
    }
}
