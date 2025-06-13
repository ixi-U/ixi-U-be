package com.ixi_U.benefit.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;

@JsonIgnoreProperties
public record SaveBundledBenefitRequest(
        @NotBlank
        String name,
        @NotBlank
        String subscript,
        @Positive
        @Min(1)
        Integer choice,
        List<String> singleBenefitIds) {

    public static SaveBundledBenefitRequest create(
            final String name,
            final String subscript,
            final Integer choice,
            final List<String> singleBenefitIds) {
        return new SaveBundledBenefitRequest(name, subscript, choice, singleBenefitIds);
    }
}