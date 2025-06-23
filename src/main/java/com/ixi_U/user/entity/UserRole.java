package com.ixi_U.user.entity;

import lombok.Getter;

@Getter
public enum UserRole {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_ANONYMOUS("anonymousUser"),
    ;

    private final String userRole;

    UserRole(String userRole) {
        this.userRole = userRole;
    }

    public static UserRole from(String role) {
        for (UserRole userRole : values()) {
            if (userRole.getUserRole().equals(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}