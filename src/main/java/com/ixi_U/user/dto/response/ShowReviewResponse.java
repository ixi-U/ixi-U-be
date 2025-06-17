package com.ixi_U.user.dto.response;

import java.time.LocalDateTime;

public record ShowReviewResponse(

        Long reviewId,
        String userName,
        int point,
        String comment,
        LocalDateTime createdAt
) {

    public static ShowReviewResponse of(
            Long reviewId,
            String userName,
            int point,
            String comment,
            LocalDateTime createdAt
    ) {
        return new ShowReviewResponse(
                reviewId,
                userName,
                point,
                comment,
                createdAt
        );
    }
}
