package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record UserAddressResponse(
        UUID id,
        UUID userId,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        boolean defaultAddress,
        Instant createdAt,
        Instant updatedAt
) {
}
