package com.bookstore.bookstore.application.command;

public record AuthRequestMetadata(
        String ipAddress,
        String userAgent,
        String deviceId,
        String deviceName
) {
    public static AuthRequestMetadata empty() {
        return new AuthRequestMetadata(null, null, null, null);
    }
}
