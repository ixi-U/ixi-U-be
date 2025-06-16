package com.ixi_U.chatbot.extractor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataLimitCondition {

    private ComparisonOperator operator;
    private int limitMb;

    @Override
    public String toString() {

        String unit = limitMb >= 1024 ? String.format("%.1fGB", limitMb / 1024.0) : limitMb + "MB";

        return String.format("%s %s", getOperatorSymbol(), unit);
    }

    private String getOperatorSymbol() {

        return switch (operator) {
            case LESS_THAN -> "<";
            case LESS_THAN_OR_EQUAL -> "≤";
            case GREATER_THAN -> ">";
            case GREATER_THAN_OR_EQUAL -> "≥";
            case EQUAL -> "=";
        };
    }
}