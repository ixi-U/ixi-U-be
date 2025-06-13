package com.ixi_U.user.exception;

import com.ixi_U.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubscribedException implements BaseException {

    PLAN_NOT_SUBSCRIBED(HttpStatus.BAD_REQUEST, "구독된 요금제가 아닙니다");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getExceptionName() {

        return this.name();
    }
}
