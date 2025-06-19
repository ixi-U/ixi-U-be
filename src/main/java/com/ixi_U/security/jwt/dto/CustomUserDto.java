package com.ixi_U.security.jwt.dto;

import com.ixi_U.user.entity.UserRole;

public record CustomUserDto(String userId, UserRole userRole) {

    public static CustomUserDto of(String userId, UserRole userRole) {

        return new CustomUserDto(userId, userRole);
    }
}