package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record CreateRoleRequest(
        @NotBlank(message = "name không được để trống")
        String name,

        @NotBlank(message = "description không được để trống")
        String description,

        Set<PermissionCode> permissionCodes
) {
}

