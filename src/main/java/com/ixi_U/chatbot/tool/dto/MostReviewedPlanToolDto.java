package com.ixi_U.chatbot.tool.dto;

import java.util.List;
import java.util.Map;

public record MostReviewedPlanToolDto(
        String id,
        String name,
        Integer mobileDataLimitMb,
        Integer sharedMobileDataLimitMb,
        Integer callLimitMinutes,
        Integer messageLimit,
        Integer monthlyPrice,
        Integer priority,
        Integer reviewedCount,
        List<Map<String, Object>> singleBenefits,
        List<Map<String, Object>> bundledBenefits) {
}