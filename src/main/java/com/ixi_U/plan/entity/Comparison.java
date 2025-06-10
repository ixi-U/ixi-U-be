package com.ixi_U.plan.entity;

public enum Comparison {

    LT("lt"),
    GT("gt"),
    EQ("eq");

    public final String comparison;

    Comparison(String comparison) {

        this.comparison = comparison;
    }
}
