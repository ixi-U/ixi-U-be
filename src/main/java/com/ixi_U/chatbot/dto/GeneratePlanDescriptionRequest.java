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

        @NotBlank
        String subscript,

        @NotNull
        int dataAmount,

        @Positive
        int monthlyPrice,

        @Valid
        @NotEmpty
        List<BundledBenefitRequest> bundledBenefits,

        @Valid
        @NotEmpty
        List<SingleBenefitRequest> singleBenefits) {

    public static GeneratePlanDescriptionRequest create(
            final String id,
            final String name,
            final String subscript,
            final int dataAmount,
            final int monthlyPrice,
            final List<BundledBenefitRequest> bundledBenefits,
            final List<SingleBenefitRequest> singleBenefits) {

        return new GeneratePlanDescriptionRequest(id, name, subscript, dataAmount, monthlyPrice, bundledBenefits,
                singleBenefits);
    }
}
