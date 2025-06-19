package com.ixi_U.security.jwt.provider.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtProviderResponseDto {

    private String accessToken;
    private String refreshToken;

    public static JwtProviderResponseDto of(String accessToken, String refreshToken) {

        return new JwtProviderResponseDto(accessToken, refreshToken);
    }
}
