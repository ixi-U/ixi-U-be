package com.ixi_U.user.dto.response;

import lombok.Builder;

@Builder
public record SubscribedResponse(
        String planName,
        String planState,
        PlanResponse plan
) {}