package com.ixi_U.user.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(

        @NotNull(message = "reviewId는 필수입니다.")
        @Min(value = 1, message = "reviewId는 1 이상의 값이어야 합니다.")
        Long reviewId,

        @NotBlank(message = "comment를 입력해 주세요")
        @Size(min = 20, max = 200, message = "comment는 최소 20자에서 200자까지 입력 가능합니다.")
        String comment
) {

    public static UpdateReviewRequest of(
            Long reviewId,
            String comment
    ) {
        return new UpdateReviewRequest(
                reviewId,
                comment
        );
    }

}
