package com.ixi_U.chatbot.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SSEEndpoint {

    String contentType() default "text/event-stream;charset=UTF-8";

    String headerType() default "Content-Type";
}
