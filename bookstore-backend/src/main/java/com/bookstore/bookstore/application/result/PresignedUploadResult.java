package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PresignedUploadResult(
        UUID fileAssetId,
        String uploadUrl,
        String method,
        Map<String, String> headers,
        Instant expiresAt,
        String storageKey
) {
}
