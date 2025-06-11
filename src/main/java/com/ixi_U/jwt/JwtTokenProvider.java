package com.ixi_U.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    // TODO: yml에서 설정하세요 네? 쉬발 뒤지기싫으면 -> 만료시간을 하드코딩하네;;;
    private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 1일

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // JWT 토큰 생성
    public String generateToken(String userId, String role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // JWT로부터 subject를 꺼내서 사용자 식별자 Id 확인
    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    // JWT로부터 role claim(토큰안의 데이터 조각) 추출
    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody().get("role", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // JWT의 모든 정보 확인
//    public Claims getClaims(String token) {
//        return Jwts.parser()
//                .verifyWith(key)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//    }

//    public Long getUserIdFromToken(String token) {
//        if (Objects.isNull(token) && !validateToken(token)) {
//            throw new GeneralException(JwtErrorCode.TOKEN_IS_EMPTY);
//        }
//
//        Claims claims = getClaims(token);
//        return Long.valueOf(claims.getSubject());
//    }
}