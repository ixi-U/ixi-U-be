package com.ixi_U.security.jwt.provider.dto;

import com.ixi_U.user.entity.UserRole;

public record JwtProviderRequestDto(String userId, UserRole userRole) {

    public static JwtProviderRequestDto of(String userId, UserRole userRole) {

        return new JwtProviderRequestDto(userId, userRole);
    }
}
