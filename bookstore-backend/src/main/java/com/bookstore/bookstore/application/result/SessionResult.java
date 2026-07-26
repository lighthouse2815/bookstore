package com.bookstore.bookstore.application.result;

import java.time.Instant;
import java.util.UUID;

public record SessionResult(
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
