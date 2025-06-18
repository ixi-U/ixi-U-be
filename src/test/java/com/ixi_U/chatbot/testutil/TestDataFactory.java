package com.ixi_U.chatbot.testutil;

import com.ixi_U.benefit.entity.BenefitType;
import com.ixi_U.chatbot.dto.BundledBenefitDTO;
import com.ixi_U.chatbot.dto.GeneratePlanDescriptionRequest;
import com.ixi_U.chatbot.dto.SingleBenefitDTO;
import com.ixi_U.plan.entity.PlanState;
import com.ixi_U.plan.entity.PlanType;

import java.util.List;
import java.util.UUID;

public class TestDataFactory {

    /**
     * 유효한 DTO
     */
    public static GeneratePlanDescriptionRequest createValidPlan() {

        SingleBenefitDTO sbr = SingleBenefitDTO.create(
                UUID.randomUUID().toString(), "테스트 단일 혜택", BenefitType.DEVICE);

        BundledBenefitDTO bbr = BundledBenefitDTO.create(
                UUID.randomUUID().toString(), "테스트 번들 혜택", 1, List.of(sbr));

        return GeneratePlanDescriptionRequest.create(
                UUID.randomUUID().toString(),
                "테스트 요금제",
                PlanState.ABLE,
                100,
                150,
                100,
                500,
                69000,
                PlanType.FIVE_G_LTE,
                "주의사항",
                500,
                7,
                30,
                false,
                null,
                null,
                null,
                List.of(sbr),
                List.of(bbr));
    }

    /**
     * NULL 필드 DTO
     */
    public static GeneratePlanDescriptionRequest createNullFieldPlan() {

        SingleBenefitDTO sbr1 = SingleBenefitDTO.create(
                UUID.randomUUID().toString(), null, BenefitType.DEVICE);
        SingleBenefitDTO sbr2 = SingleBenefitDTO.create(
                UUID.randomUUID().toString(), null, BenefitType.DEVICE);

        BundledBenefitDTO bbr = BundledBenefitDTO.create(
                UUID.randomUUID().toString(), "테스트 번들 혜택", 1, List.of(sbr1));

        return GeneratePlanDescriptionRequest.create(
                null,
                "테스트 요금제",
                PlanState.ABLE,
                100,
                150,
                100,
                500,
                69000,
                PlanType.FIVE_G_LTE,
                "주의사항",
                500,
                7,
                30,
                false,
                null,
                null,
                null,
                List.of(sbr2),
                List.of(bbr));
    }
}