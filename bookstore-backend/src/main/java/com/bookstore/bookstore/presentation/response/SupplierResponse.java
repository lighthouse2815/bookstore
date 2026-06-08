package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String name,
        String phone,
        String email,
        String address,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}
