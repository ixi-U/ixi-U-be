package com.ixi_U.security.exception;

import com.ixi_U.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SecurityException implements BaseException {

    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "토큰을 찾을 수 없습니다."),
    COOKIE_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠키를 찾을 수 없습니다."),
    INVALID_REGISTRATION_ID(HttpStatus.NOT_FOUND, "유효하지 않은 소셜 로그인 방식입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getExceptionName() {
        return name();
    }
}