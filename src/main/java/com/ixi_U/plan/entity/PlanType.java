package com.ixi_U.plan.entity;

import lombok.Getter;

@Getter
public enum PlanType {

    FIVE_G_LTE("5G/LTE"),
    ONLINE("ONLINE"),
    TABLET_SMARTWATCH("TABLET/SMARTWATCH"),
    DUAL_NUMBER("DUAL_NUMBER");

    private final String plan_type;

    PlanType(String plan_type) {

        this.plan_type = plan_type;
    }
}
