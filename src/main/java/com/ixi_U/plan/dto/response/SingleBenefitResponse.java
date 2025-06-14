package com.ixi_U.plan.dto.response;

import com.ixi_U.benefit.entity.SingleBenefit;

public record SingleBenefitResponse(String name, String description, String benefitType) {

    public static SingleBenefitResponse from(SingleBenefit singleBenefit) {

        return new SingleBenefitResponse(singleBenefit.getName(), singleBenefit.getDescription(),
                singleBenefit.getBenefitType().getType());
    }
}
