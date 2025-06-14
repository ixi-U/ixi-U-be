package com.ixi_U.chatbot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record BundledBenefitDTO(

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
        List<SingleBenefitDTO> singleBenefits) {

    public static BundledBenefitDTO create(
            final String id,
            final String name,
            final String subscript,
            final int choice,
            final List<SingleBenefitDTO> singleBenefits) {

        return new BundledBenefitDTO(id, name, subscript, choice, singleBenefits);
    }
}
