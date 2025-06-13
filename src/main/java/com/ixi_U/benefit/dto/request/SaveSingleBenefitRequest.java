package com.ixi_U.benefit.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ixi_U.benefit.entity.BenefitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties
public record SaveSingleBenefitRequest(
        @NotBlank
        String name,
        @NotBlank
        String subscript,
        @NotNull
        BenefitType benefitType) {

    public static SaveSingleBenefitRequest create(
            final String name,
            final String subscript,
            final BenefitType benefitType) {
        return new SaveSingleBenefitRequest(name, subscript, benefitType);
    }
}
