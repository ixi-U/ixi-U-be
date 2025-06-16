package com.ixi_U.chatbot.extractor;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPriceCondition {

    private ComparisonOperator operator;
    private int amount;

    @Override
    public String toString() {

        return String.format("%s %,d원", getOperatorSymbol(), amount);
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