package com.bookstore.bookstore.application.result;

import java.time.Instant;

public record SignedUrlResult(
        String url,
        Instant expiresAt
) {
}
