package com.ixi_U.plan.service;

import com.ixi_U.benefit.entity.BundledBenefit;
import com.ixi_U.benefit.entity.SingleBenefit;
import com.ixi_U.benefit.repository.BundledBenefitRepository;
import com.ixi_U.benefit.repository.SingleBenefitRepository;
import com.ixi_U.chatbot.dto.BundledBenefitDTO;
import com.ixi_U.chatbot.dto.GeneratePlanDescriptionRequest;
import com.ixi_U.chatbot.dto.SingleBenefitDTO;
import com.ixi_U.chatbot.service.VectorService;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.PlanNameDto;
import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.dto.request.GetPlansRequest;
import com.ixi_U.plan.dto.request.SavePlanRequest;
import com.ixi_U.plan.dto.response.*;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.entity.PlanSortOption;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final BundledBenefitRepository bundledBenefitRepository;
    private final SingleBenefitRepository singleBenefitRepository;
    private final VectorService vectorService;

    /**
     * 요금제 저장 & 벡터 저장소에 저장
     */
    @Transactional
    public PlanEmbeddedResponse savePlan(final SavePlanRequest request) {

        validateSamePlanName(request);

        List<BundledBenefit> bundledBenefits = bundledBenefitRepository.findAllById(request.bundledBenefits());

        log.info("bundledBenefits = {}", bundledBenefits);

        List<SingleBenefit> singleBenefits = singleBenefitRepository.findAllById(request.singleBenefits());

        log.info("singleBenefits = {}", singleBenefits);

        Plan newPlan = Plan.of(request.name(), request.mobileDataLimitMb(),
                request.sharedMobileDataLimitMb(), request.callLimitMinutes(),
                request.messageLimit(), request.monthlyPrice(), request.type(),
                request.usageCautions(), request.mobileDataThrottleSpeedKbps(), request.minAge(),
                request.maxAge(), request.isActiveDuty(), request.pricePerKb(), request.etcInfo(),
                request.priority(), bundledBenefits, singleBenefits);

        Plan save = planRepository.save(newPlan);

        GeneratePlanDescriptionRequest generatePlanDescriptionRequest = planEntityToDto(save);

        return vectorService.saveEmbeddedPlan(generatePlanDescriptionRequest);
    }

    private void validateSamePlanName(final SavePlanRequest request) {

        if (planRepository.existsByName(request.name())) {
            throw new GeneralException(PlanException.PLAN_NAME_DUPLE);
        }
    }

    private GeneratePlanDescriptionRequest planEntityToDto(Plan plan) {

        return GeneratePlanDescriptionRequest.create(
                plan.getId(),
                plan.getName(),
                plan.getMobileDataLimitMb(),
                plan.getMonthlyPrice(),
                bundledBenefitEntityToDto(plan.getBundledBenefits()),
                singleBenefitEntityToDto(plan.getSingleBenefits())
        );
    }

    private List<BundledBenefitDTO> bundledBenefitEntityToDto(List<BundledBenefit> bundledBenefits) {

        return bundledBenefits.stream()
                .map(entity ->
                        BundledBenefitDTO.create(
                                entity.getId(),
                                entity.getName(),
                                entity.getChoice(),
                                singleBenefitEntityToDto(entity.getSingleBenefits()
                                )
                        )).toList();
    }

    private List<SingleBenefitDTO> singleBenefitEntityToDto(List<SingleBenefit> singleBenefits) {

        return singleBenefits.stream()
                .map(entity ->
                        SingleBenefitDTO.create(
                                entity.getId(),
                                entity.getName(),
                                entity.getBenefitType()
                        )
                ).toList();
    }

    @Transactional(readOnly = true)
    public SortedPlanResponse findPlans(Pageable pageable, GetPlansRequest request) {

        PlanType planType = PlanType.from(request.planTypeStr());
        PlanSortOption planSortOption = PlanSortOption.from(request.planSortOptionStr());
        Slice<PlanSummaryDto> plans = planRepository.findPlans(pageable, planType, planSortOption,
                request.searchKeyword(), request.planId(), request.cursorSortValue());

        String lastPlanId = getLastPlanId(plans);
        int lastSortValue = getLastSortValue(plans, planSortOption);

        return new SortedPlanResponse(plans, lastPlanId, lastSortValue);
    }

    private String getLastPlanId(Slice<PlanSummaryDto> plans) {

        if (!plans.hasNext()) {

            return null;
        }
        List<PlanSummaryDto> content = plans.getContent();

        return content.get(content.size() - 1).id();
    }

    private int getLastSortValue(Slice<PlanSummaryDto> plans, PlanSortOption sortOption) {

        if (!plans.hasNext()) {

            return 0;
        }
        List<PlanSummaryDto> content = plans.getContent();
        PlanSummaryDto last = content.get(content.size() - 1);

        return PlanSortOption.extractSortValue(last, sortOption);
    }

    @Transactional(readOnly = true)
    public PlanDetailResponse findPlanDetail(String planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new GeneralException(PlanException.PLAN_NOT_FOUND));

        return PlanDetailResponse.from(plan);
    }

    public List<PlanNameDto> getPlanNameList() {
        return planRepository.findAllPlanNames();
    }
}
