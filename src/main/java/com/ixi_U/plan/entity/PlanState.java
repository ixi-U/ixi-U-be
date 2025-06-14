package com.ixi_U.plan.entity;

import lombok.Getter;

@Getter
public enum PlanState {

    ABLE("ABLE"),
    DISABLE("DISABLE");

    private final String state;

    PlanState(String state){

        this.state = state;
    }
}
