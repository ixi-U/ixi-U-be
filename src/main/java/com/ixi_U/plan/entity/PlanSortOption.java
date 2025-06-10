package com.ixi_U.plan.entity;

import com.ixi_U.common.exception.GeneralException;
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

	public static PlanSortOption from(String key) {

		return switch (key.toLowerCase()) {
			case "priority" -> PRIORITY;
			case "priceasc" -> PRICE_ASC;
			case "pricedesc" -> PRICE_DESC;
			case "mobiledatalimitmb" -> DATA_DESC;
			default -> throw new GeneralException(PlanException.INVALID_SORT_VALUE);
		};
	}
}
