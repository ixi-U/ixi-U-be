package com.ixi_U.plan.dto;

import java.util.List;
import java.util.Map;

public record PlanListDto(String id, String name, String mobileDataLimitMb,
                          String sharedMobileDataLimitMb,
                          String callLimitMinutes,
                          String messageLimit,
                          int monthlyPrice,
                          int priority,
                          List<Map<String, Object>> singleBenefits,
                          List<Map<String, Object>> bundledBenefits) {

}
