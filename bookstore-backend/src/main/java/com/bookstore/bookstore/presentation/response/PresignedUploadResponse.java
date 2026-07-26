package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PresignedUploadResponse(
        UUID fileAssetId,
        String uploadUrl,
        String method,
        Map<String, String> headers,
        Instant expiresAt,
        String storageKey
) {
}
