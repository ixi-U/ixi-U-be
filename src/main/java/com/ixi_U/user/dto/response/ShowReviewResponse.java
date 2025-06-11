package com.ixi_U.user.dto.response;

public record ShowReviewResponse(
        String userName,
        int point,
        String comment
) {

    public static ShowReviewResponse of(
            String userName,
            int point,
            String comment
    ) {
        return new ShowReviewResponse(
                userName,
                point,
                comment
        );
    }
}
