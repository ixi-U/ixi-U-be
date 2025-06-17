package com.ixi_U.user.dto.response;

import com.ixi_U.user.entity.UserRole;
import java.time.LocalDate;

public record ShowMyInfoResponse(
        String id,
        String name,
        String email,
        UserRole userRole,
        LocalDate createdAt
) {

    public static ShowMyInfoResponse of(
            String id,
            String name,
            String email,
            UserRole userRole,
            LocalDate createdAt) {

        return new ShowMyInfoResponse(id, name, email, userRole, createdAt);
    }
}
