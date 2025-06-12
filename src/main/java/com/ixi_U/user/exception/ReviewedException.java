package com.ixi_U.user.exception;

import com.ixi_U.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewedException implements BaseException {

    REVIEW_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 요금제에 대해 리뷰를 작성하였습니다");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getExceptionName() {

        return this.name();
    }
}
