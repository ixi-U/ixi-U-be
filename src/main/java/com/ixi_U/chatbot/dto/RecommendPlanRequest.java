package com.ixi_U.chatbot.dto;

public record RecommendPlanRequest(String userQuery) {

    public static RecommendPlanRequest create(final String userQuery){

        return new RecommendPlanRequest(userQuery);
    }
}
