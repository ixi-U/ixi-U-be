package com.ixi_U.auth.exception;

//public class KakaoAuthException extends RuntimeException {
//    public KakaoAuthException(String message) {
//        super(message);
//    }
//}

import com.ixi_U.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum KakaoAuthException implements BaseException {

    TOKEN_ISSUE_FAILED(HttpStatus.UNAUTHORIZED, "카카오 access token 발급에 실패했습니다."),
    USER_INFO_REQUEST_FAILED(HttpStatus.UNAUTHORIZED, "카카오 사용자 정보 조회에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getExceptionName() {
        return this.name();
    }
}

