package com.ixi_U.plan.service;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.dto.response.SortedPlanResponse;
import com.ixi_U.plan.entity.PlanSortOption;
import com.ixi_U.plan.entity.PlanType;
import com.ixi_U.plan.exception.PlanException;
import com.ixi_U.plan.repository.PlanRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public SortedPlanResponse findPlans(Pageable pageable, String planTypeStr, String sortOptionStr,
            String searchKeyword, String planId, Integer sortValue) {

        PlanType planType = PlanType.from(planTypeStr);
        PlanSortOption planSortOption = PlanSortOption.from(sortOptionStr);
        Slice<PlanSummaryDto> plans = planRepository.findPlans(pageable, planType, planSortOption,
                searchKeyword, planId, sortValue);

        String lastPlanId = getLastPlanId(plans);
        int lastSortValue = getLastSortValue(plans, planSortOption);

        return SortedPlanResponse.of(plans, lastPlanId, lastSortValue);
    }

    private int extractSortValue(PlanSummaryDto planSummary, PlanSortOption sortOption) {

        return switch (sortOption.getField()) {
            case "priority" -> planSummary.priority();
            case "monthlyPrice" -> planSummary.monthlyPrice();
            case "mobileDataLimitMb" -> planSummary.mobileDataLimitMb();
            default -> throw new GeneralException(PlanException.INVALID_SORT_VALUE);
        };
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

        return extractSortValue(last, sortOption);
    }
}
