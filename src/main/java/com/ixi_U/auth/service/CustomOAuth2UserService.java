package com.ixi_U.auth.service;

import com.ixi_U.auth.dto.CustomOAuth2User;
import com.ixi_U.user.entity.User;
import com.ixi_U.user.entity.UserRole;
import com.ixi_U.user.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

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
        String email = kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString()
                : "temp_kakao_" + kakaoId + "@example.com";

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> userRepository.save(
                        User.of(nickname, email, "kakao", kakaoId, UserRole.ROLE_USER)));

        return new CustomOAuth2User(nickname, user.getId(), user.getUserRole());
    }
}