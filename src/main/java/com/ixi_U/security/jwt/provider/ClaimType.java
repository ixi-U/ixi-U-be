package com.ixi_U.security.jwt.provider;

import lombok.Getter;

@Getter
public enum ClaimType {

    USER_ID("userId"),
    CATEGORY("category"),
    USER_ROLE("userRole");

    private final String key;

    ClaimType(String key) {
        this.key = key;
    }
}
