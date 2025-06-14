package com.ixi_U.chatbot.dto;

import com.ixi_U.benefit.entity.BenefitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SingleBenefitRequest(

        @NotBlank
        String id,

        @NotBlank
        String name,

        @NotBlank
        String subscript,

        @NotNull
        BenefitType benefitType) {

    public static SingleBenefitRequest create(
            final String id,
            final String name,
            final String subscript,
            final BenefitType benefitType) {

        return new SingleBenefitRequest(id, name, subscript, benefitType);
    }
}
