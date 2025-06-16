package com.ixi_U.chatbot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record GeneratePlanDescriptionRequest(

        @NotBlank
        String id,

        @NotBlank
        String name,

        @NotNull
        int mobileDataLimitMb,

        @Positive
        int monthlyPrice,

        @Valid
        @NotEmpty
        List<BundledBenefitDTO> bundledBenefits,

        @Valid
        @NotEmpty
        List<SingleBenefitDTO> singleBenefits) {

    public static GeneratePlanDescriptionRequest create(
            final String id,
            final String name,
            final int mobileDataLimitMb,
            final int monthlyPrice,
            final List<BundledBenefitDTO> bundledBenefits,
            final List<SingleBenefitDTO> singleBenefits) {

        return new GeneratePlanDescriptionRequest(id, name, mobileDataLimitMb, monthlyPrice, bundledBenefits, singleBenefits);
    }
}
