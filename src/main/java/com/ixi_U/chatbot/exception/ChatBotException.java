package com.ixi_U.chatbot.exception;

import com.ixi_U.common.exception.BaseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatBotException implements BaseException {

    CHAT_BOT_EMBEDDING_DESCRIPTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "임베딩을 위한 Description 생성에서 오류가 발생했습니다.")
    ,CHAT_BOT_FILTER_EXPRESSION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "필터 표현식 추출에서 오류가 발생했습니다.")
    ,CHAT_BOT_RECOMMENDING_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "추천 응답에서 오류가 발생했습니다.")
    ,CHAT_BOT_FORBIDDEN_WORD_DETECT(HttpStatus.SERVICE_UNAVAILABLE,"챗봇 응답에서 비속어가 발견되었습니다. 주의 부탁드립니다");

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
