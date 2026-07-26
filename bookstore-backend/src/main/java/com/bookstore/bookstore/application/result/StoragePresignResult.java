package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.Map;

public record StoragePresignResult(
        String url,
        String method,
        Map<String, String> headers,
        Instant expiresAt
) {
}
