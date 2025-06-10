package com.ixi_U.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @NotBlank(message = "planId를 입력해 주세요")
        String planId,

        @Min(value = 1, message = "point는 최소 1점 이상이어야 합니다.")
        @Max(value = 5, message = "point는 최대 5점 이하여야 합니다.")
        int point,

        @NotBlank(message = "comment를 입력해 주세요")
        @Size(min = 20, max = 200, message = "comment는 최소 20자에서 200자까지 입력 가능합니다.")
        String comment
) {

    public static CreateReviewRequest of(
            String planId,
            int point,
            String comment
    ) {
        return new CreateReviewRequest(
                planId,
                point,
                comment
        );
    }

}
