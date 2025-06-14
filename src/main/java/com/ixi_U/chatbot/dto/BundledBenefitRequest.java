package com.ixi_U.chatbot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record BundledBenefitRequest(

        @NotBlank
        String id,

        @NotBlank
        String name,

        @NotBlank
        String subscript,

        @Positive
        @NotNull
        int choice,

        @Valid
        @NotEmpty
        List<SingleBenefitRequest> singleBenefits) {

    public static BundledBenefitRequest create(
            final String id,
            final String name,
            final String subscript,
            final int choice,
            final List<SingleBenefitRequest> singleBenefits) {

        return new BundledBenefitRequest(id, name, subscript, choice, singleBenefits);
    }
}
