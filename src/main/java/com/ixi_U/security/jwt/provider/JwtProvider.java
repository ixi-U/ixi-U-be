package com.ixi_U.security.jwt.provider;

import com.ixi_U.security.jwt.JwtKeyManager;
import com.ixi_U.security.jwt.JwtType;
import com.ixi_U.security.jwt.provider.dto.JwtProviderRequestDto;
import com.ixi_U.security.jwt.provider.dto.JwtProviderResponseDto;
import com.ixi_U.user.entity.UserRole;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtKeyManager jwtKeyManager;

    public JwtProviderResponseDto generateAccessTokenAndRefreshToken(JwtProviderRequestDto dto) {

        String accessToken = generateToken(dto.userId(), dto.userRole(), JwtType.ACCESS_TOKEN);
        String refreshToken = generateToken(dto.userId(), dto.userRole(), JwtType.REFRESH_TOKEN);

        return JwtProviderResponseDto.of(accessToken, refreshToken);
    }

    private String generateToken(String userId, UserRole role, JwtType token) {

        Date exp = new Date(System.currentTimeMillis() + token.getExpiredTime());

        return Jwts.builder()
                .claim(ClaimType.CATEGORY.getKey(), token.getCategory())
                .claim(ClaimType.USER_ID.getKey(), userId)
                .claim(ClaimType.USER_ROLE.getKey(), role.getUserRole())
                .expiration(exp)
                .signWith(jwtKeyManager.getSecretKey(), Jwts.SIG.HS512)
                .compact();
    }
}
