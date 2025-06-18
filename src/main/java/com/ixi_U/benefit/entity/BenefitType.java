package com.ixi_U.benefit.entity;

import lombok.Getter;

@Getter
public enum BenefitType {

    DEVICE("기기 혜택"),
    DISCOUNT("할인 혜택"),
    SUBSCRIPTION("구독 혜택");

    private final String type;

    BenefitType(String type) {

        this.type = type;
    }
}