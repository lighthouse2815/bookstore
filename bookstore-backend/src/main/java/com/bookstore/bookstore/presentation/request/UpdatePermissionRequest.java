package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import jakarta.validation.constraints.NotNull;

public record UpdatePermissionRequest(
        @NotNull(message = "code khong duoc null")
        PermissionCode code,

        String description
) {
}
