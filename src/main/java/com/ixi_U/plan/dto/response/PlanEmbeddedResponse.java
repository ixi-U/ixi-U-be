package com.ixi_U.plan.dto.response;

import java.util.Map;

public record PlanEmbeddedResponse(String description, Map<String, Object> metaData) {

    public static PlanEmbeddedResponse create(final String description, final Map<String, Object> metaData) {

        return new PlanEmbeddedResponse(description, metaData);
    }
}
