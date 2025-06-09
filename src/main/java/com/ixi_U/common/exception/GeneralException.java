package com.ixi_U.common.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class GeneralException extends RuntimeException {

    private final BaseException exception;

    public String getExceptionName() {

        return exception.getExceptionName();
    }

    public HttpStatus getHttpStatus() {

        return exception.getHttpStatus();
    }

    public String getMessage() {

        return exception.getMessage();
    }
}
