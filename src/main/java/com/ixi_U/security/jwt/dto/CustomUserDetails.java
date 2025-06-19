package com.ixi_U.security.jwt.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * SNS 로그인이 아닌 일반 로그인 또는 회원가입을 통해 생성되는 객체
 * 사용자에게 토큰이 있을 경우 토큰 정보를 바탕으로 Authentication 객체 생성
 */
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final CustomUserDto customUserDto;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(customUserDto.userRole().getUserRole()));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }

    public String getUserId() {
        return customUserDto.userId();
    }

}
