package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(
        UUID id,
        PermissionCode code,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
