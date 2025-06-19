package com.ixi_U.security.oauth2.dto;

import lombok.Getter;

@Getter
public enum Provider {

    GITHUB("github"),
    GOOGLE("google"),
    NAVER("naver"),
    KAKAO("kakao");

    private final String provider;

    Provider(String provider) {
        this.provider = provider;
    }
}
