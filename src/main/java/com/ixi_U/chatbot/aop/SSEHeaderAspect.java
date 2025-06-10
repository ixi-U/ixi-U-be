package com.ixi_U.chatbot.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SSEHeaderAspect {

    @Around("@annotation(sseEndpoint)")
    public Object handleReactiveResponse(ProceedingJoinPoint joinPoint, SSEEndpoint sseEndpoint)
            throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof ResponseEntity<?> responseEntity) {
            return ResponseEntity.ok()
                    .headers(responseEntity.getHeaders())
                    .header(sseEndpoint.headerType(), sseEndpoint.contentType())
                    .body(responseEntity.getBody());
        }
        return result;
    }
}
