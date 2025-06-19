package com.ixi_U.security.jwt;

import lombok.Getter;

@Getter
public enum JwtType {
    ACCESS_TOKEN("ACCESS_TOKEN", 1000 * 10),// 1000초 * 10초
    REFRESH_TOKEN("REFRESH_TOKEN", 1000 * 60 * 60 * 24 * 7); // 1000(1초) * 60(1분) * 60(60분) * 24(24시간) * 7(7일)

    private final String category;
    private final int expiredTime;

    JwtType(String category, int expiredTime) {
        this.category = category;
        this.expiredTime = expiredTime;
    }
}
