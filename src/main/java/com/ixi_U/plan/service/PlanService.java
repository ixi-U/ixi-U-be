package com.ixi_U.plan.service;

import com.ixi_U.benefit.entity.BundledBenefit;
import com.ixi_U.benefit.entity.SingleBenefit;
import com.ixi_U.benefit.repository.BundledBenefitRepository;
import com.ixi_U.benefit.repository.SingleBenefitRepository;
import com.ixi_U.chatbot.dto.BundledBenefitDTO;
import com.ixi_U.chatbot.dto.GeneratePlanDescriptionRequest;
import com.ixi_U.chatbot.dto.SingleBenefitDTO;
import com.ixi_U.chatbot.service.VectorStoreService;
import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.PlanListDto;
import com.ixi_U.plan.dto.PlanNameDto;
import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.dto.request.GetPlansRequest;
import com.ixi_U.plan.dto.request.SavePlanRequest;
import com.ixi_U.plan.dto.response.*;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.entity.PlanSortOption;
import com.ixi_U.plan.entity.PlanState;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final BundledBenefitRepository bundledBenefitRepository;
    private final SingleBenefitRepository singleBenefitRepository;
    private final VectorStoreService vectorStoreService;

    public void embedAllPlan() {

        List<Plan> all = planRepository.findAll();

        List<GeneratePlanDescriptionRequest> requests = new ArrayList<>();

        for (Plan plan : all) {
            requests.add(planEntityToDto(plan));
        }

        vectorStoreService.embedAllPlan(requests);
    }

    /**
     * 요금제 저장 & 벡터 저장소에 저장
     */
    @Transactional
    @CacheEvict(
            value = {"planListPages", "planCounts"},
            allEntries = true
    )
    public PlanEmbeddedResponse savePlan(final SavePlanRequest request) {

        validateSamePlanName(request);

        List<BundledBenefit> bundledBenefits = bundledBenefitRepository.findAllById(
                request.bundledBenefits());

        log.info("bundledBenefits = {}", bundledBenefits);

        List<SingleBenefit> singleBenefits = singleBenefitRepository.findAllById(
                request.singleBenefits());

        log.info("singleBenefits = {}", singleBenefits);

        Plan newPlan = Plan.of(request.name(), request.mobileDataLimitMb(),
                request.sharedMobileDataLimitMb(), request.callLimitMinutes(),
                request.messageLimit(), request.monthlyPrice(), request.type(),
                request.usageCautions(), request.mobileDataThrottleSpeedKbps(), request.minAge(),
                request.maxAge(), request.isActiveDuty(), request.pricePerKb(), request.etcInfo(),
                request.priority(), bundledBenefits, singleBenefits);

        Plan save = planRepository.save(newPlan);

        GeneratePlanDescriptionRequest generatePlanDescriptionRequest = planEntityToDto(save);

        return vectorStoreService.saveEmbeddedPlan(generatePlanDescriptionRequest);
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
                plan.getPlanState(),
                plan.getMobileDataLimitMb(),
                plan.getSharedMobileDataLimitMb(),
                plan.getCallLimitMinutes(),
                plan.getMessageLimit(),
                plan.getMonthlyPrice(),
                plan.getPlanType(),
                plan.getUsageCautions(),
                plan.getMobileDataThrottleSpeedKbps(),
                plan.getMinAge(),
                plan.getMaxAge(),
                plan.getIsActiveDuty(),
                plan.getPricePerKb(),
                plan.getEtcInfo(),
                plan.getPriority(),
                singleBenefitEntityToDto(plan.getSingleBenefits()),
                bundledBenefitEntityToDto(plan.getBundledBenefits())
        );
    }

    private List<BundledBenefitDTO> bundledBenefitEntityToDto(
            List<BundledBenefit> bundledBenefits) {
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
    @Cacheable(
            value = "planListPages",
            key = "'planListPages:' + #pageable.pageNumber + '-' + #request.planTypeStr() + '-' + #request.planSortOptionStr() + '-' + #request.searchKeyword() + '-' + (#request.planId() != null ? #request.planId() : '') + '-' + #request.cursorSortValue()"
    )
    public SortedPlanResponse findPlans(Pageable pageable, GetPlansRequest request) {

        PlanType planType = PlanType.from(request.planTypeStr());
        PlanSortOption planSortOption = PlanSortOption.from(request.planSortOptionStr());
        Slice<PlanSummaryDto> plans = planRepository.findPlans(pageable, planType, planSortOption,
                request.searchKeyword(), request.planId(), request.cursorSortValue());

        List<PlanListDto> convertedPlanList = plans.getContent().stream()
                .map(dto -> new PlanListDto(
                        dto.id(),
                        dto.name(),
                        convertMobileData(dto.mobileDataLimitMb()),
                        convertSharedMobileDataLimitMb(dto.sharedMobileDataLimitMb()),
                        convertCallLimit(dto.callLimitMinutes()),
                        convertMessageLimit(dto.messageLimit()),
                        dto.monthlyPrice(),
                        dto.priority(),
                        dto.singleBenefits(),
                        dto.bundledBenefits()
                ))
                .toList();

        boolean hasNext = plans.hasNext();
        String lastPlanId = getLastPlanId(plans);
        int lastSortValue = getLastSortValue(plans, planSortOption);

        return new SortedPlanResponse(convertedPlanList, hasNext, lastPlanId, lastSortValue);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "planCounts", key = "'all'")
    public PlansCountResponse countPlans() {

        return PlansCountResponse.from(planRepository.countPlans());
    }

    private String convertCallLimit(int callLimitMinutes) {
        return callLimitMinutes == Integer.MAX_VALUE ? "기본제공"
                : String.valueOf(callLimitMinutes) + " 분";
    }

    private String convertMessageLimit(int messageLimit) {
        return messageLimit == Integer.MAX_VALUE ? "기본제공" : String.valueOf(messageLimit) + " 건";
    }

    private String convertMobileData(int mobileDataLimitMb) {
        return mobileDataLimitMb == Integer.MAX_VALUE ? "무제한"
                : String.valueOf(mobileDataLimitMb / 1000) + " GB";
    }

    private String convertSharedMobileDataLimitMb(int sharedMobileDataLimitMb) {
        return sharedMobileDataLimitMb == Integer.MAX_VALUE ? "무제한"
                : String.valueOf(sharedMobileDataLimitMb / 1000) + " GB";
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

    public List<PlanAdminResponse> getPlansForAdmin() {
        return planRepository.findAllForAdmin().stream()
                .map(PlanAdminResponse::from)
                .toList();
    }

    @CacheEvict(
            value = {"planListPages", "planCounts"},
            allEntries = true
    )
    public void togglePlanState(String planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("해당 요금제를 찾을 수 없습니다."));

        Plan updated = plan.withPlanState(
                plan.getPlanState() == PlanState.ABLE ? PlanState.DISABLE : PlanState.ABLE
        );

        planRepository.save(updated);
    }

    @Transactional
    @CacheEvict(
            value = {"planListPages", "planCounts"},
            allEntries = true
    )
    public void disablePlan(String planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new GeneralException(PlanException.PLAN_NOT_FOUND));

        Plan updatePlan;

        if (plan.getPlanState() == PlanState.ABLE) {
            updatePlan = plan.withPlanState(PlanState.DISABLE);
        } else {
            updatePlan = plan.withPlanState(PlanState.ABLE);
        }
        planRepository.save(updatePlan);
    }

    public List<PlanNameDto> getPlanNameList() {
        return planRepository.findAllPlanNames();
    }
}
