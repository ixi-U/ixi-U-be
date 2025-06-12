package com.ixi_U.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ExceptionResponse> handleGeneralException(GeneralException e) {

        return ResponseEntity.status(e.getHttpStatus())
                .body(new ExceptionResponse(e.getExceptionName(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(
            MethodArgumentNotValidException e) {

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("잘못된 요청입니다.");

        return ResponseEntity.badRequest()
                .body(new ExceptionResponse("VALIDATION_ERROR", errorMessage));
    }
}
