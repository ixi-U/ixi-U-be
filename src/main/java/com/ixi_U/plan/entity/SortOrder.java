package com.ixi_U.plan.entity;

public enum SortOrder {

    ASC("asc"),
    DESC("desc");

    public final String order;

    SortOrder(String order) {

        this.order = order;
    }
}
