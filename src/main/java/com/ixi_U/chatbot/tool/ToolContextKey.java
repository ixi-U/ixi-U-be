package com.ixi_U.chatbot.tool;

import lombok.Getter;

@Getter
public enum ToolContextKey {

    USER_ID("userId"),
    FILTER_EXPRESSION("filterExpression")
    ;

    private final String key;

    ToolContextKey(String key){
        this.key = key;
    }
}
