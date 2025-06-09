package com.ixi_U.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseException {

    String getExceptionName();
    HttpStatus getHttpStatus();
    String getMessage();
}
