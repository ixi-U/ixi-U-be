package com.ixi_U.chatbot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record BundledBenefitDTO(

        @NotBlank
        String id,

        @NotBlank
        String name,

        @Positive
        int choice,

        @Valid
        List<SingleBenefitDTO> singleBenefits) {

    public static BundledBenefitDTO create(
            final String id,
            final String name,
            final int choice,
            final List<SingleBenefitDTO> singleBenefits) {

        return new BundledBenefitDTO(id, name, choice, singleBenefits);
    }
}
