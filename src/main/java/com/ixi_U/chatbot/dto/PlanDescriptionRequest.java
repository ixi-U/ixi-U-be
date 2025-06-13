package com.ixi_U.chatbot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanDescriptionRequest {

    @NotBlank
    private final String id;

    @NotBlank
    private final String name;

    @NotBlank
    private final String subscript;

    @NotNull
    private final Integer data;

    @Positive
    private final Integer price;

    @Valid
    @NotEmpty
    private final List<BundledBenefitRequest> bundledBenefits;

    @Valid
    @NotEmpty
    private final List<SingleBenefitRequest> singleBenefits;

    public static PlanDescriptionRequest of(final String id, final String name,
            final String subscript,
            final int data, final int price,
            final List<BundledBenefitRequest> bundledBenefits,
            final List<SingleBenefitRequest> singleBenefits) {
        return PlanDescriptionRequest.builder()
                .id(id)
                .name(name)
                .subscript(subscript)
                .data(data)
                .price(price)
                .bundledBenefits(bundledBenefits)
                .singleBenefits(singleBenefits)
                .build();
    }
}
