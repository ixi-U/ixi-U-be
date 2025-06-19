package com.ixi_U.security.utils;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.security.exception.SecurityException;
import com.ixi_U.security.jwt.JwtType;
import com.ixi_U.security.jwt.provider.dto.JwtProviderResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.WebUtils;

@Slf4j
public class CookieUtils {

    public static void setTokenCookie(JwtProviderResponseDto jwtProviderResponseDto, HttpServletResponse response) {

        setResponse(JwtType.ACCESS_TOKEN.getCategory(), jwtProviderResponseDto.getAccessToken(), JwtType.ACCESS_TOKEN.getExpiredTime(), response);
        setResponse(JwtType.REFRESH_TOKEN.getCategory(), jwtProviderResponseDto.getRefreshToken(), JwtType.REFRESH_TOKEN.getExpiredTime(), response);
    }

    private static void setResponse(String key, String value, int expiredTime, HttpServletResponse response) {

        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(expiredTime);
        response.addCookie(cookie);
    }

    public static JwtProviderResponseDto getAccessTokenAndRefreshToken(HttpServletRequest request) {

        Cookie accessTokenCookie = WebUtils.getCookie(request, JwtType.ACCESS_TOKEN.getCategory());
        Cookie refreshTokenCookie = WebUtils.getCookie(request, JwtType.REFRESH_TOKEN.getCategory());

        validateCookie(accessTokenCookie, refreshTokenCookie);

        String accessToken = accessTokenCookie.getValue();
        String refreshToken = refreshTokenCookie.getValue();

        log.info("accessTokenCookie = {}", accessTokenCookie.getValue());
        log.info("refreshTokenCookie = {}", refreshTokenCookie.getValue());
        validateTokenIsNull(accessToken, refreshToken);

        return JwtProviderResponseDto.of(accessToken, refreshToken);
    }

    private static void validateTokenIsNull(String accessToken, String refreshToken) {

        if (accessToken == null) throw new GeneralException(SecurityException.TOKEN_NOT_FOUND);
        if (refreshToken == null) throw new GeneralException(SecurityException.TOKEN_NOT_FOUND);
    }

    private static void validateCookie(Cookie accessTokenCookie, Cookie refreshTokenCookie) {

        if (accessTokenCookie == null) throw new GeneralException(SecurityException.COOKIE_NOT_FOUND);
        if (refreshTokenCookie == null) throw new GeneralException(SecurityException.COOKIE_NOT_FOUND);
    }
}