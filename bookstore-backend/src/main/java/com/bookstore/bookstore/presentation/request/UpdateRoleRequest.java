package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record UpdateRoleRequest(
        @NotBlank(message = "name khong duoc de trong")
        String name,

        @NotBlank(message = "description khong duoc de trong")
        String description,

        Set<PermissionCode> permissionCodes
) {
}
