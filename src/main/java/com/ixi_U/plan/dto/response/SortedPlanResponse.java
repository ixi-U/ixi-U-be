package com.ixi_U.plan.dto.response;

import com.ixi_U.plan.dto.PlanSummaryDto;
import org.springframework.data.domain.Slice;

public record SortedPlanResponse(Slice<PlanSummaryDto> plans, String lastPlanId,
                                 int lastSortValue) {

}
