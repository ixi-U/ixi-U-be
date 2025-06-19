package com.ixi_U.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SecurityExceptionResponseSetter {
    private static final String IDENTITY = "security -> ";
    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";
    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private final ObjectMapper objectMapper = new ObjectMapper();

    //CustomAuthenticationEntryPoint에 사용됨
    public void setResponse(HttpServletResponse response, HttpStatus httpStatus, Exception authException) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.setStatus(httpStatus.value());
        setExceptionBody(response, httpStatus, authException.getMessage());
    }

    //CustomAuthenticationEntryPoint에 사용됨
    public void setResponse(HttpServletResponse response, HttpStatus httpStatus, String message) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.setStatus(httpStatus.value());
        setExceptionBody(response, httpStatus, message);
    }

    private void setExceptionBody(HttpServletResponse response, HttpStatus httpStatus, String message) throws IOException {
        Map<Object, Object> errorBody = new HashMap<>();
        errorBody.put(CODE, httpStatus.value());
        errorBody.put(MESSAGE, IDENTITY + message);
        objectMapper.writeValue(response.getWriter(), errorBody);
    }
}