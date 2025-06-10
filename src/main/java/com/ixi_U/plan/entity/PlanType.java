package com.ixi_U.plan.entity;

import lombok.Getter;

@Getter
public enum PlanType {

    FIVE_G_LTE("5G/LTE"),
    ONLINE("ONLINE"),
    TABLET_SMARTWATCH("TABLET/SMARTWATCH"),
    DUAL_NUMBER("DUAL_NUMBER");

    private final String planType;

    PlanType(String planType) {

        this.planType = planType;
    }

    public static PlanType from(String type) {

        return switch (type) {
            case "5G/LTE" -> FIVE_G_LTE;
            case "ONLINE" -> ONLINE;
            case "TABLET/SMARTWATCH" -> TABLET_SMARTWATCH;
            case "DUAL_NUMBER" -> DUAL_NUMBER;
            default -> throw new IllegalArgumentException("Invalid sort option");
        };
    }
}
