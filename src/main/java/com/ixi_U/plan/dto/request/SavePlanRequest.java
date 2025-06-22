package com.ixi_U.plan.dto.request;

import com.ixi_U.plan.entity.PlanState;
import com.ixi_U.plan.entity.PlanType;

import java.util.List;

public record SavePlanRequest(
        String name,
        PlanState state,
        Integer mobileDataLimitMb,
        Integer sharedMobileDataLimitMb,
        Integer callLimitMinutes,
        Integer messageLimit,
        Integer monthlyPrice,
        PlanType type,
        String usageCautions,
        Integer mobileDataThrottleSpeedKbps,
        Integer minAge,
        Integer maxAge,
        Boolean isActiveDuty,
        Double pricePerKb,
        String etcInfo,
        Integer priority,
        List<String> singleBenefits,
        List<String> bundledBenefits) {

    public static SavePlanRequest create(
            final String name,
            final PlanState state,
            final Integer mobileDataLimitMb,
            final Integer sharedMobileDataLimitMb,
            final Integer callLimitMinutes,
            final Integer messageLimit,
            final Integer monthlyPrice,
            final PlanType type,
            final String usageCautions,
            final Integer mobileDataThrottleSpeedKbps,
            final Integer minAge,
            final Integer maxAge,
            final Boolean isActiveDuty,
            final Double pricePerKb,
            final String etcInfo,
            final Integer priority,
            final List<String> singleBenefits,
            final List<String> bundledBenefits) {
        return new SavePlanRequest(
                name,
                state,
                mobileDataLimitMb,
                sharedMobileDataLimitMb,
                callLimitMinutes,
                messageLimit,
                monthlyPrice,
                type,
                usageCautions,
                mobileDataThrottleSpeedKbps,
                minAge,
                maxAge,
                isActiveDuty,
                pricePerKb,
                etcInfo,
                priority,
                singleBenefits,
                bundledBenefits
        );
    }

}
