package com.ixi_U.plan.dto.response;

import com.ixi_U.plan.dto.PlansCountDto;
import lombok.Builder;

@Builder
public record PlansCountResponse(int all, int fiveGLte, int online, int tabletSmartwatch,
                                 int dualNumber) {

    public static PlansCountResponse from(PlansCountDto plansCountDto) {

        return PlansCountResponse.builder()
                .all(plansCountDto.all())
                .fiveGLte(plansCountDto.fiveGLte())
                .online(plansCountDto.online())
                .tabletSmartwatch(plansCountDto.tabletSmartwatch())
                .dualNumber(plansCountDto.dualNumber())
                .build();
    }
}
