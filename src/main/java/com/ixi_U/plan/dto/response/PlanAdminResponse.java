package com.ixi_U.plan.dto.response;

import com.ixi_U.plan.entity.Plan;
import com.ixi_U.plan.entity.PlanState;

public record PlanAdminResponse(
        String id,
        String name,
        PlanState planState,
        String usageCautions
) {

    public static PlanAdminResponse from(Plan plan) {

        return new PlanAdminResponse(
                plan.getId(),
                plan.getName(),
                plan.getPlanState(),
                plan.getUsageCautions()
        );
    }
}