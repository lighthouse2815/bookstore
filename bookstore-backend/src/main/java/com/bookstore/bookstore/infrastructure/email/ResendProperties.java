package com.bookstore.bookstore.infrastructure.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.resend")
public record ResendProperties(
        String baseUrl,
        String apiKey,
        String fromEmail,
        String fromName
) {
}
