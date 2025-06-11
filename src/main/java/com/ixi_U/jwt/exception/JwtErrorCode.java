package com.ixi_U.jwt.exception;

import com.ixi_U.common.exception.runtime.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode implements ErrorCode {
    TOKEN_IS_EMPTY("JWT TOKEN이 비어있습니다.", "JWTTOKEN_001"),
    INVALID_TOKEN("유효하지 않은 JWT TOKEN 입니다.", "JWTTOKEN_002"),
    SIBAL_NO("하기싫은 작업입니다. 니가 해~", "JWTTOKEN_003");

    private final String message;
    private final String code;

}