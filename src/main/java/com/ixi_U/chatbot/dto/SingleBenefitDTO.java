package com.ixi_U.chatbot.dto;

import com.ixi_U.benefit.entity.BenefitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SingleBenefitDTO(

        @NotBlank
        String id,

        @NotBlank
        String name,

        @NotBlank
        String subscript,

        @NotNull
        BenefitType benefitType) {

    public static SingleBenefitDTO create(
            final String id,
            final String name,
            final String subscript,
            final BenefitType benefitType) {

        return new SingleBenefitDTO(id, name, subscript, benefitType);
    }
}
