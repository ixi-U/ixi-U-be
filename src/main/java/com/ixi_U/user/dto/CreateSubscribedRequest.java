package com.ixi_U.user.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSubscribedRequest(@NotBlank(message = "유효하지 않은 ID") String planId) {

}
