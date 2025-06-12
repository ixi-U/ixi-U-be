package com.ixi_U.plan.entity;

import com.ixi_U.common.exception.GeneralException;
import com.ixi_U.plan.dto.PlanSummaryDto;
import com.ixi_U.plan.exception.PlanException;
import lombok.Getter;

@Getter
public enum PlanSortOption {

    PRIORITY("priority", SortOrder.DESC, Comparison.LT),
    PRICE_ASC("monthlyPrice", SortOrder.ASC, Comparison.GT),
    PRICE_DESC("monthlyPrice", SortOrder.DESC, Comparison.LT),
    DATA_DESC("mobileDataLimitMb", SortOrder.DESC, Comparison.LT);

    public final String field;
    public final SortOrder order;
    public final Comparison comparison;

    PlanSortOption(String field, SortOrder order, Comparison comparison) {

        this.field = field;
        this.order = order;
        this.comparison = comparison;
    }

    public static PlanSortOption from(String planSortOptionStr) {

        validateSortOption(planSortOptionStr);

        return switch (planSortOptionStr) {
            case "PRIORITY" -> PRIORITY;
            case "PRICE_ASC" -> PRICE_ASC;
            case "PRICE_DESC" -> PRICE_DESC;
            case "DATA_DESC" -> DATA_DESC;
            default -> throw new GeneralException(PlanException.INVALID_SORT_VALUE);
        };
    }

    public static int extractSortValue(PlanSummaryDto planSummary, PlanSortOption sortOption) {

        return switch (sortOption.getField()) {
            case "priority" -> planSummary.priority();
            case "monthlyPrice" -> planSummary.monthlyPrice();
            case "mobileDataLimitMb" -> planSummary.mobileDataLimitMb();
            default -> throw new GeneralException(PlanException.INVALID_SORT_VALUE);
        };
    }

    private static void validateSortOption(String sortOptionStr) {

        if (sortOptionStr == null || sortOptionStr.isBlank()) {

            throw new GeneralException(PlanException.INVALID_SORT_VALUE);
        }
    }
}
