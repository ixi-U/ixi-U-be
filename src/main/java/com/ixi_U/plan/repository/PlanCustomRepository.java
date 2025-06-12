package com.ixi_U.plan.repository;

import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.entity.PlanSortOption;
import com.ixi_U.plan.entity.PlanType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PlanCustomRepository {

    Slice<PlanSummaryDto> findPlans(Pageable pageable, PlanType planType,
            PlanSortOption planSortOptionStr, String searchKeyword, String planId,
            Integer cursorSortValue);

}
