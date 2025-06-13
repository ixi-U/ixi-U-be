package com.ixi_U.chatbot.testutil;

import com.ixi_U.benefit.entity.BenefitType;
import com.ixi_U.chatbot.dto.BundledBenefitRequest;
import com.ixi_U.chatbot.dto.PlanDescriptionRequest;
import com.ixi_U.chatbot.dto.SingleBenefitRequest;
import java.util.List;
import java.util.UUID;

public class TestDataFactory {

    /**
     * 유효한 DTO
     */
    public static PlanDescriptionRequest createValidPlan() {

        SingleBenefitRequest sbr = SingleBenefitRequest.create(
                UUID.randomUUID().toString(), "테스트 단일 혜택", "기본 단일 혜택", BenefitType.DEVICE);

        BundledBenefitRequest bbr = BundledBenefitRequest.of(
                UUID.randomUUID().toString(), "테스트 번들 혜택", "기본 번들 혜택", 1, List.of(sbr));

        return PlanDescriptionRequest.of(UUID.randomUUID().toString(), "테스트 요금제", "기본 요금제", 100,
                59000, List.of(bbr), List.of(sbr));
    }

    /**
     * NULL 필드 DTO
     */
    public static PlanDescriptionRequest createNullFieldPlan() {

        SingleBenefitRequest sbr1 = SingleBenefitRequest.create(
                UUID.randomUUID().toString(), null, null, BenefitType.DEVICE);
        SingleBenefitRequest sbr2 = SingleBenefitRequest.create(
                UUID.randomUUID().toString(), null, null, BenefitType.DEVICE);

        BundledBenefitRequest bbr = BundledBenefitRequest.of(
                UUID.randomUUID().toString(), "테스트 번들 혜택", "기본 번들 혜택", 1, List.of(sbr1));

        return PlanDescriptionRequest.of(UUID.randomUUID().toString(), "테스트 요금제", "기본 요금제", 100,
                59000, List.of(bbr), List.of(sbr2));
    }
}