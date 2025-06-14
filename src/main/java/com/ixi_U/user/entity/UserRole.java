package com.ixi_U.user.entity;

import lombok.Getter;

@Getter
public enum UserRole {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");

    private final String userRole;

    UserRole(String userRole) {
        this.userRole = userRole;
    }
}