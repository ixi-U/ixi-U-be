package com.ixi_U.plan.dto.response;

import com.ixi_U.plan.dto.PlanListDto;
import org.springframework.data.domain.Slice;

public record SortedPlanResponse(Slice<PlanListDto> plans, String lastPlanId,
                                 int lastSortValue) {

}
