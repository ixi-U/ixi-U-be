package com.ixi_U.plan.dto;

import lombok.Builder;

@Builder
public record PlansCountDto(int all, int fiveGLte, int online, int tabletSmartwatch,
                            int dualNumber) {

    public static PlansCountDto of(int all, int fiveGLte, int online, int tabletSmartwatch,
            int dualNumber) {

        return PlansCountDto.builder()
                .all(all)
                .fiveGLte(fiveGLte)
                .online(online)
                .tabletSmartwatch(tabletSmartwatch)
                .dualNumber(dualNumber)
                .build();
    }
}
