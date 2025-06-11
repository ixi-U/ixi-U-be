package com.ixi_U.user.dto.response;

import java.util.List;

public record ShowReviewListResponse(
        List<ShowReviewResponse> reviewResponseList,
        boolean hasNextPage
) {

    public static ShowReviewListResponse of(
            List<ShowReviewResponse> reviewResponseList,
            boolean hasNextPage
    ) {
        return new ShowReviewListResponse(
                reviewResponseList,
                hasNextPage
        );
    }

}
