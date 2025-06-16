package com.ixi_U.chatbot.exception;

import com.ixi_U.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatBotException implements BaseException {

    CHAT_BOT_BAD_RESPONSE(HttpStatus.SERVICE_UNAVAILABLE, "LLM 응답이 유효하지 않습니다."),
    CHAT_BOT_PRICE_EXTRACTOR_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "가격 키워드 추출에서 오류가 발생했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getExceptionName() {
        return name();
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
