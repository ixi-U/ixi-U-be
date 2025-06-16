package com.ixi_U.user.dto.response;

import java.time.LocalDateTime;

public record ShowSubscribedHistoryResponse(
        Long subscribedId,
        String planName,
        LocalDateTime subscribedAt
) {

    public static ShowSubscribedHistoryResponse from(com.ixi_U.user.entity.Subscribed subscribed) {
        return new ShowSubscribedHistoryResponse(
                subscribed.getId(),
                subscribed.getPlan().getName(),
                subscribed.getCreatedAt()
        );
    }
}