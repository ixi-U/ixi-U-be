package com.ixi_U.benefit.entity;

import lombok.Getter;

@Getter
public enum BenefitType {

    DEVICE("스마트 기기"),
    DISCOUNT("할인"),
    SUBSCRIPTION("구독");

    private final String type;

    BenefitType(String type) {

        this.type = type;
    }
}