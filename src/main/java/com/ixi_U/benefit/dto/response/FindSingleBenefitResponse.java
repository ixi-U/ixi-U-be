package com.ixi_U.benefit.dto.response;

import com.ixi_U.benefit.entity.BenefitType;

public record FindSingleBenefitResponse(
        String id,
        String name,
        String subscript,
        BenefitType benefitType) {

    public static FindSingleBenefitResponse create(
            final String id,
            final String name,
            final String subscript,
            final BenefitType benefitType) {
        return new FindSingleBenefitResponse(id, name, subscript, benefitType);
    }
}
