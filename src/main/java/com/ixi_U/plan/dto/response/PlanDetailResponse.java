package com.ixi_U.plan.dto.response;

import com.ixi_U.plan.entity.Plan;
import java.util.List;

public record PlanDetailResponse(String name, int mobileDataLimitMb, int sharedMobileDataLimitMb,
                                 int callLimitMinutes, int messageLimit, int monthlyPrice,
                                 String planType, String usageCautions,
                                 int mobileDataThrottleSpeedKbps, int minAge, int maxAge,
                                 int pricePerKb, String etcInfo,
                                 List<BundledBenefitResponse> bundledBenefits,
                                 List<SingleBenefitResponse> singleBenefits) {

    public static PlanDetailResponse from(Plan plan) {

        List<BundledBenefitResponse> bundledBenefits = plan.getBundledBenefits().stream()
                .map(BundledBenefitResponse::from)
                .toList();

        List<SingleBenefitResponse> singleBenefits = plan.getSingleBenefits().stream()
                .map(SingleBenefitResponse::from)
                .toList();

        return new PlanDetailResponse(plan.getName(), plan.getMobileDataLimitMb(),
                plan.getSharedMobileDataLimitMb(),
                plan.getCallLimitMinutes(), plan.getMessageLimit(), plan.getMonthlyPrice(),
                plan.getPlanType().name(), plan.getUsageCautions(),
                plan.getMobileDataThrottleSpeedKbps(),
                plan.getMinAge(), plan.getMaxAge(), plan.getPricePerKb(), plan.getEtcInfo(),
                bundledBenefits, singleBenefits);
    }
}
