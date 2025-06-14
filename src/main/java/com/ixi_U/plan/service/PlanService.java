package com.ixi_U.plan.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.dto.request.GetPlansRequest;
import com.ixi_U.plan.dto.response.PlanDetailResponse;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.entity.PlanSortOption;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.repository.PlanRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

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
}
