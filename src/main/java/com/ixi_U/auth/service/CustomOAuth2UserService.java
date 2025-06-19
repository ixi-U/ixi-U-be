package com.ixi_U.auth.service;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * 카카오로부터 사용자 정보를 받아오는 부분
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long kakaoId = Long.valueOf(attributes.get("id").toString());

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = profile.get("nickname").toString();
        // 임시 이메일 생성 : 카카오에서 이메일 못가져옴
        String email = kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString()
                : "temp_kakao_" + kakaoId + "@example.com";

        // 신규/기존 유저에 따라 redirect 위치 달라짐
        Optional<User> optionalUser = userRepository.findByKakaoId(kakaoId);
        boolean isNewUser = optionalUser.isEmpty();

        User user = optionalUser.orElseGet(() ->
                userRepository.save(User.of(nickname, email, "kakao", kakaoId, UserRole.ROLE_USER))
        );

        return new CustomOAuth2User(nickname, user.getId(), user.getUserRole(),
                isNewUser); // JWT 토큰 생성에 사용되는 사용자 정보의 출처가 된다.
    }

    public void expireCookie(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
    }

    public OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long kakaoId = Long.valueOf(attributes.get("id").toString());

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = profile.get("nickname").toString();
        String email = kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString()
                : "temp_kakao_" + kakaoId + "@example.com";

        Optional<User> optionalUser = userRepository.findByKakaoId(kakaoId);
        boolean isNewUser = optionalUser.isEmpty();

        User user = optionalUser.orElseGet(() ->
                userRepository.save(User.of(nickname, email, "kakao", kakaoId, UserRole.ROLE_USER))
        );

        return new CustomOAuth2User(nickname, user.getId(), user.getUserRole(), isNewUser);
    }

    public OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long kakaoId = Long.valueOf(attributes.get("id").toString());

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = profile.get("nickname").toString();
        String email = kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString()
                : "temp_kakao_" + kakaoId + "@example.com";

        Optional<User> optionalUser = userRepository.findByKakaoId(kakaoId);
        boolean isNewUser = optionalUser.isEmpty();

        User user = optionalUser.orElseGet(() ->
                userRepository.save(User.of(nickname, email, "kakao", kakaoId, UserRole.ROLE_USER))
        );

        return new CustomOAuth2User(nickname, user.getId(), user.getUserRole(), isNewUser);
    }
}


