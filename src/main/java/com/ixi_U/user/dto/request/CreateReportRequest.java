package com.ixi_U.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(

        @NotNull(message = "reviewId는 필수입니다.")
        @Min(value = 1, message = "reviewId는 1 이상의 값이어야 합니다.")
        Long reviewId
) {

    public static CreateReportRequest from(
            Long reviewId
    ) {
        return new CreateReportRequest(
                reviewId
        );
    }

}

