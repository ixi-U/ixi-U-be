package com.ixi_U.plan;

import lombok.Getter;

@Getter
public enum State {

    ABLE("ABLE"),
    DISABLE("DISABLE");

    private final String state;

    State(String state){

        this.state = state;
    }
}
