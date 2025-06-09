package com.ixi_U.benefit.entity;

import lombok.Getter;

@Getter
public enum BenefitType {

    DEVICE("DEVICE"),
    DISCOUNT("DISCOUNT"),
    SUBSCRIPTION("SUBSCRIPTION");

    private final String type;

    BenefitType(String type) {

        this.type = type;
    }
}