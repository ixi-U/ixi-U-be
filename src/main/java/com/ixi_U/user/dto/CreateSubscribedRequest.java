package com.ixi_U.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSubscribedRequest(
        @NotBlank
        String planId
) {

}
