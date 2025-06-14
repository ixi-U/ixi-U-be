package com.ixi_U.plan.dto.response;

import com.ixi_U.benefit.entity.BundledBenefit;
import java.util.List;

public record BundledBenefitResponse(String name, String description, int choice,
                                     List<SingleBenefitResponse> singleBenefits) {

    public static BundledBenefitResponse from(BundledBenefit bundledBenefit) {

        List<SingleBenefitResponse> singleBenefits = bundledBenefit.getSingleBenefits()
                .stream()
                .map(SingleBenefitResponse::from)
                .toList();

        return new BundledBenefitResponse(bundledBenefit.getName(), bundledBenefit.getSubscript(),
                bundledBenefit.getChoice(), singleBenefits);
    }
}
