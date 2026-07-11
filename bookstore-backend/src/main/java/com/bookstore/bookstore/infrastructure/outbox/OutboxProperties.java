package com.bookstore.bookstore.infrastructure.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.outbox")
public record OutboxProperties(
        boolean enabled,
        long delayMs,
        int batchSize,
        int maxAttempts,
        long processingTimeoutSeconds,
        long backlogWarningThreshold
) {
    public OutboxProperties {
        if (delayMs < 1000L) delayMs = 1000L;
        if (batchSize < 1 || batchSize > 500) batchSize = 100;
        if (maxAttempts < 1 || maxAttempts > 50) maxAttempts = 8;
        if (processingTimeoutSeconds < 30L) processingTimeoutSeconds = 300L;
        if (backlogWarningThreshold < 1L) backlogWarningThreshold = 1000L;
    }
}
