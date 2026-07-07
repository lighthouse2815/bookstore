package com.bookstore.bookstore.presentation.response;

import java.time.Instant;

public record SignedUrlResponse(
        String url,
        Instant expiresAt
) {
}
