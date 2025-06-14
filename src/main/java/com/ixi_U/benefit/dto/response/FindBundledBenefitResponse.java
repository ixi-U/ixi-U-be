package com.ixi_U.benefit.dto.response;

import java.util.List;

public record FindBundledBenefitResponse(
        String id,
        String name,
        String subscript,
        Integer choice,
        List<FindSingleBenefitResponse> singleBenefitResponses) {

    public static FindBundledBenefitResponse create(
            final String id,
            final String name,
            final String subscript,
            final Integer choice,
            final List<FindSingleBenefitResponse> singleBenefitResponses) {
        return new FindBundledBenefitResponse(id, name, subscript, choice, singleBenefitResponses);
    }
}
