package com.ixi_U.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ExceptionResponse> handleGeneralException(GeneralException e) {

        return ResponseEntity.status(e.getHttpStatus())
                .body(new ExceptionResponse(e.getExceptionName(), e.getMessage()));
    }
}
