package com.ixi_U.chatbot.testutil;

import com.ixi_U.benefit.entity.BenefitType;
import com.ixi_U.chatbot.dto.BundledBenefitDTO;
import com.ixi_U.chatbot.dto.GeneratePlanDescriptionRequest;
import com.ixi_U.chatbot.dto.SingleBenefitDTO;
import java.util.List;
import java.util.UUID;

public class TestDataFactory {

    /**
     * 유효한 DTO
     */
    public static GeneratePlanDescriptionRequest createValidPlan() {

        SingleBenefitDTO sbr = SingleBenefitDTO.create(
                UUID.randomUUID().toString(), "테스트 단일 혜택", "기본 단일 혜택", BenefitType.DEVICE);

        BundledBenefitDTO bbr = BundledBenefitDTO.create(
                UUID.randomUUID().toString(), "테스트 번들 혜택", "기본 번들 혜택", 1, List.of(sbr));

        return GeneratePlanDescriptionRequest.create(UUID.randomUUID().toString(), "테스트 요금제", 100,
                59000, List.of(bbr), List.of(sbr));
    }

    /**
     * NULL 필드 DTO
     */
    public static GeneratePlanDescriptionRequest createNullFieldPlan() {

        SingleBenefitDTO sbr1 = SingleBenefitDTO.create(
                UUID.randomUUID().toString(), null, null, BenefitType.DEVICE);
        SingleBenefitDTO sbr2 = SingleBenefitDTO.create(
                UUID.randomUUID().toString(), null, null, BenefitType.DEVICE);

        BundledBenefitDTO bbr = BundledBenefitDTO.create(
                UUID.randomUUID().toString(), "테스트 번들 혜택", "기본 번들 혜택", 1, List.of(sbr1));

        return GeneratePlanDescriptionRequest.create(UUID.randomUUID().toString(), "테스트 요금제", 100,
                59000, List.of(bbr), List.of(sbr2));
    }
}