package com.ixi_U.plan.dto.response;

import com.ixi_U.plan.dto.PlanListDto;
import java.util.List;

public record SortedPlanResponse(List<PlanListDto> plans,
                                 boolean hasNext,
                                 String lastPlanId,
                                 int lastSortValue) {

}