package com.ixi_U.chatbot.dto;

import com.ixi_U.benefit.entity.BenefitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanDescriptionRequest {

    @NotBlank
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String subscript;

    @NotEmpty
    private int data;

    @NotEmpty
    private int price;

    private List<BundledBenefitRequest> bundledBenefits;

    private List<SingleBenefitRequest> singleBenefits;

    public static PlanDescriptionRequest of(final String id, final String name, final String subscript,
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

    public static PlanDescriptionRequest createValidPlan() {

        SingleBenefitRequest sbr = SingleBenefitRequest.of(
                UUID.randomUUID().toString(), "테스트 단일 혜택", "기본 단일 혜택", BenefitType.DEVICE);

        BundledBenefitRequest bbr = BundledBenefitRequest.of(
                UUID.randomUUID().toString(), "테스트 번들 혜택", "기본 번들 혜택", 1, List.of(sbr));

        return PlanDescriptionRequest.of(UUID.randomUUID().toString(), "테스트 요금제", "기본 요금제", 100,
                59000, List.of(bbr), List.of(sbr));
    }
}
