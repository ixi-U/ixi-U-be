package com.ixi_U.chatbot.dto;

import com.ixi_U.plan.entity.PlanState;
import com.ixi_U.plan.entity.PlanType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record GeneratePlanDescriptionRequest(

        @NotBlank
        String id,

        @NotBlank
        String name,

        @NotNull
        PlanState state,

        Integer mobileDataLimitMb,

        Integer shareMobileDataLimitMb,

        Integer callLimitMinutes,

        Integer messageLimit,

        @Positive
        Integer monthlyPrice,

        PlanType type,

        String usageCautions,

        Integer mobileDataThrottleSpeedKbps,

        Integer minAge,

        Integer maxAge,

        Boolean isActiveDuty,

        Integer pricePerKb,

        String etcInfo,

        Integer priority,

        @Valid
        List<SingleBenefitDTO> singleBenefits,

        @Valid
        List<BundledBenefitDTO> bundledBenefits) {

    public static GeneratePlanDescriptionRequest create(
            final String id,
            final String name,
            final PlanState planState,
            final Integer mobileDataLimitMb,
            final Integer shareMobileDataLimitMb,
            final Integer callLimitMinutes,
            final Integer messageLimit,
            final Integer monthlyPrice,
            final PlanType planType,
            final String usageCautions,
            final Integer mobileDataThrottleSpeedKbps,
            final Integer minAge,
            final Integer maxAge,
            final Boolean isActiveDuty,
            final Integer pricePerKb,
            final String etcInfo,
            final Integer priority,
            final List<SingleBenefitDTO> singleBenefits,
            final List<BundledBenefitDTO> bundledBenefits) {

        return new GeneratePlanDescriptionRequest(
                id,
                name,
                planState,
                mobileDataLimitMb,
                shareMobileDataLimitMb,
                callLimitMinutes,
                messageLimit,
                monthlyPrice,
                planType,
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
