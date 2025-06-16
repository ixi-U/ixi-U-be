package com.ixi_U.plan.dto;

import java.util.List;
import java.util.Map;

public record PlanSummaryDto(String id, String name, int mobileDataLimitMb,
                             int sharedMobileDataLimitMb,
                             int callLimitMinutes,
                             int messageLimit,
                             int monthlyPrice,
                             int priority,
                             List<Map<String, Object>> singleBenefits,
                             List<Map<String, Object>> bundledBenefits) {

}
