package com.ixi_U.plan.exception;

import com.ixi_U.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlanException implements BaseException {

    INVALID_SORT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 정렬 조건입니다."),
    INVALID_PLAN_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 요금제 타입입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "유효하지 않은 파라미터입니다.");
    PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "요금제를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getExceptionName() {

        return this.name();
    }
}
