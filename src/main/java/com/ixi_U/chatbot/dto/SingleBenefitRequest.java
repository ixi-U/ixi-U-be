package com.ixi_U.chatbot.dto;

import com.ixi_U.benefit.entity.BenefitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SingleBenefitRequest {

    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String subscript;

    @NotNull
    private BenefitType benefitType;

    public static SingleBenefitRequest of(final String id, final String name,
            final String subscript,
            final BenefitType benefitType) {
        return SingleBenefitRequest.builder()
                .id(id)
                .name(name)
                .subscript(subscript)
                .benefitType(benefitType)
                .build();
    }
}
