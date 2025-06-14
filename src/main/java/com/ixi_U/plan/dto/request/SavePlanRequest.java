package com.ixi_U.plan.dto.request;


import com.ixi_U.plan.entity.PlanState;
import com.ixi_U.plan.entity.PlanType;
import java.util.List;

public record SavePlanRequest(
        String name,
        PlanState state,
        int mobileDataLimitMb,
        int sharedMobileDataLimitMb,
        int callLimitMinutes,
        int messageLimit,
        int monthlyPrice,
        PlanType type,
        String usageCautions,
        int mobileDataThrottleSpeedKbps,
        int minAge,
        int maxAge,
        boolean isActiveDuty,
        int pricePerKb,
        String etcInfo,
        int priority,
        List<String> singleBenefits,
        List<String> bundledBenefits) {

    public static SavePlanRequest create(
            final String name,
            final PlanState state,
            final int mobileDataLimitMb,
            final int sharedMobileDataLimitMb,
            final int callLimitMinutes,
            final int messageLimit,
            final int monthlyPrice,
            final PlanType type,
            final String usageCautions,
            final int mobileDataThrottleSpeedKbps,
            final int minAge,
            final int maxAge,
            final boolean isActiveDuty,
            final int pricePerKb,
            final String etcInfo,
            final int priority,
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
