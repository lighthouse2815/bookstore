package com.bookstore.bookstore.presentation.response;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID sessionId,
        String deviceName,
        String deviceId,
        String userAgent,
        String ipAddress,
        Instant createdAt,
        Instant lastUsedAt,
        Instant expiresAt,
        boolean currentSession
) {}
