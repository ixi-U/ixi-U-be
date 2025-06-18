package com.ixi_U.jwt;

import com.ixi_U.user.entity.UserRole;
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

    @Value("${jwt.access-token-expiration-time}")
    private long ACCESS_TOKEN_EXP;

    @Value("${jwt.refresh-token-expiration-days}")
    private int REFRESH_TOKEN_EXP;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // JWT token 생성
    public String generateAccessToken(String userId, UserRole userRole) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", userRole.getUserRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXP))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP))
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
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
//            return true;
//        } catch (JwtException e) {
//            return false;
//        }
//    }
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("❌ JWT 만료: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("❌ 지원하지 않는 JWT: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("❌ JWT 형식 오류: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("❌ JWT 서명 오류: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("❌ JWT 파라미터 오류 (null 또는 공백): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ 기타 오류: " + e.getMessage());
        }
        return false;
    }

    public long getAccessTokenExp() {
        return ACCESS_TOKEN_EXP;
    }

    public long getRefreshTokenExp() {
        return REFRESH_TOKEN_EXP;
    }
}