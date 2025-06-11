package com.ixi_U.chatbot.exception;

import com.ixi_U.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatBotException implements BaseException {

    CHAT_BOT_BAD_RESPONSE(HttpStatus.SERVICE_UNAVAILABLE, "LLM 응답이 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getExceptionName() {
        return "";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
