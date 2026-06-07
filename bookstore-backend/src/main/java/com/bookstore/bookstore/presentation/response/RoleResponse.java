package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        Set<PermissionCode> permissionCodes,
        Instant createdAt,
        Instant updatedAt
) {
}
