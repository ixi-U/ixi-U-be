package com.ixi_U.user.dto.response;

public record ShowReviewSummaryResponse(
        ShowReviewStatsResponse showReviewStatsResponse,
        ShowReviewResponse myReviewResponse
) {

    public static ShowReviewSummaryResponse of(
            ShowReviewStatsResponse showReviewStatsResponse,
            ShowReviewResponse myReviewResponse
    ){

        return new ShowReviewSummaryResponse(
                showReviewStatsResponse,
                myReviewResponse
        );
    }

}
