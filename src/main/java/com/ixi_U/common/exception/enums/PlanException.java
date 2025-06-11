package com.ixi_U.common.exception.enums;

import com.ixi_U.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlanException implements BaseException {

    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "요금제를 찾을 수 없습니다."),
    ALREADY_SUBSCRIBED_PLAN(HttpStatus.BAD_REQUEST, "이미 현재 구독 중인 요금제입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getExceptionName() {

        return this.name();
    }

    @Override
    public HttpStatus getHttpStatus() {

        return this.httpStatus;
    }

    @Override
    public String getMessage() {

        return this.message;
    }

}
