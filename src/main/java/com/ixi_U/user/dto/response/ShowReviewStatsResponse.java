package com.ixi_U.user.dto.response;

public record ShowReviewStatsResponse(

        double averagePoint,
        int totalCount
) {

    public static ShowReviewStatsResponse of(
            double averagePoint,
            int totalCount
    ) {

        return new ShowReviewStatsResponse(
                averagePoint,
                totalCount
        );
    }

}
