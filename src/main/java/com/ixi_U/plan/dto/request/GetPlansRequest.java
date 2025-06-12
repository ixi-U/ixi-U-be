package com.ixi_U.plan.dto.request;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.exception.PlanException;

public record GetPlansRequest(String planTypeStr, String planSortOptionStr,
                              String searchKeyword, String planId, Integer cursorSortValue) {

    public static GetPlansRequest of(String planTypeStr, String planSortOptionStr,
            String searchKeyword, String planId, Integer cursorSortValue) {

        return new GetPlansRequest(planTypeStr, planSortOptionStr, searchKeyword, planId,
                cursorSortValue);
    }

    public static void validate(int size) {

        if (size < 1) {
            throw new GeneralException(PlanException.INVALID_PARAMETER);
        }
    }

}
