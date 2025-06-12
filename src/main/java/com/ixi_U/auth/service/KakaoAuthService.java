package com.ixi_U.auth.service;

import com.ixi_U.auth.dto.KakaoUserResponse;
import com.ixi_U.auth.dto.KakaoTokenResponse;
import com.ixi_U.auth.exception.KakaoAuthException;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.jwt.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;

    // 1. 카카오 인가코드 요청
    public String getAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", restApiKey);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoTokenResponse> response =
                    restTemplate.postForEntity(tokenUrl, request, KakaoTokenResponse.class);
            return response.getBody().access_token();
        } catch (HttpClientErrorException e) {
            log.error("카카오 access token 발급 실패: {}", e.getResponseBodyAsString());
            throw new GeneralException(KakaoAuthException.TOKEN_ISSUE_FAILED);
        }
    }

    public String loginAndIssueJwt(String code) {
        String kakaoAccessToken = getAccessToken(code); // 2. 카카오 서버로 카카오 access_token 발급
        KakaoUserResponse kakaoUser = getUserInfoFromKakao(kakaoAccessToken); // 3. 카카오 access_token으로 유저 정보 확인

        handleUserLoginFlow(kakaoUser); // 4. 카카오 유저 정보를 기반으로 신규/기존 사용자 확인 후 신규면 DB 저장

        // 5. 카카오 유저를 구분하는 고유 Pk 값으로 user를 찾을 수 없다면 error
        User user = userRepository.findByKakaoId(kakaoUser.id())
                .orElseThrow(() -> new GeneralException(KakaoAuthException.USER_NOT_FOUND));

        // 6. 사용자 확인되면 jwt 토큰 발급
        return jwtTokenProvider.generateToken(user.getId(), "ROLE_USER");
    }

    // 4. 카카오 유저 정보를 기반으로 신규/기존 사용자 확인 후 신규면 DB 저장
    public boolean handleUserLoginFlow(KakaoUserResponse kakaoUser) {
        Long kakaoId = kakaoUser.id();
        Optional<User> optionalUser = userRepository.findByKakaoId(kakaoId);

        if (optionalUser.isPresent()) return false;

        String nickname = kakaoUser.getNickname();
        String email = "temp_kakao_" + kakaoId + "@example.com"; // todo 프론트에서 이메일 입력 요청 (필수?)

//        if (email == null) {
//            throw new GeneralException(KakaoAuthException.EMAIL_NOT_FOUND);
//        }

        User newUser = User.of(nickname, email, "kakao", kakaoId);
        userRepository.save(newUser);
        return true;
    }

    public KakaoUserResponse getUserInfoFromKakao(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserResponse> response =
                restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, KakaoUserResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new GeneralException(KakaoAuthException.USER_INFO_REQUEST_FAILED);
        }

        return response.getBody();
    }
}
