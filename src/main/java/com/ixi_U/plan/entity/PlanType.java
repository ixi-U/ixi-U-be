package com.ixi_U.plan.entity;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.exception.PlanException;
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

        if (type == null) {

            return null;
        }

        validatePlanType(type);

        return switch (type) {
            case "5G/LTE" -> FIVE_G_LTE;
            case "ONLINE" -> ONLINE;
            case "TABLET/SMARTWATCH" -> TABLET_SMARTWATCH;
            case "DUAL_NUMBER" -> DUAL_NUMBER;
            default -> throw new GeneralException(PlanException.INVALID_PLAN_TYPE);
        };
    }

    private static void validatePlanType(String planTypeStr) {

        if (planTypeStr.isBlank()) {

            throw new GeneralException(PlanException.INVALID_PLAN_TYPE);
        }
    }
}
