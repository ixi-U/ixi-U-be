package com.ixi_U.security.oauth2.dto;

import com.ixi_U.user.entity.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * CustomOAuth2UserService 에서 사용되는 클래스
 * SNS 로그인을 통해 Authentication 객체를 생성하기위한 클래스
 */
@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2UserDto oAuth2UserDto;
    private final String userId;
    private final UserRole userRole;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(userRole.getUserRole()));
    }

    @Override
    public String getName() {
        return oAuth2UserDto.name();
    }
}
