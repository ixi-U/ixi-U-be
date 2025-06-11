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
public class BundledBenefitRequest {

    @NotBlank
    private final String id;

    @NotBlank
    private final String name;

    @NotBlank
    private final String subscript;

    @Positive
    @NotNull
    private final Integer choice;

    @Valid
    @NotEmpty
    private List<SingleBenefitRequest> singleBenefits;

    public static BundledBenefitRequest of(final String id, final String name,
            final String subscript,
            final int choice, final List<SingleBenefitRequest> singleBenefits) {
        return BundledBenefitRequest.builder()
                .id(id)
                .name(name)
                .subscript(subscript)
                .choice(choice)
                .singleBenefits(singleBenefits)
                .build();
    }
}
