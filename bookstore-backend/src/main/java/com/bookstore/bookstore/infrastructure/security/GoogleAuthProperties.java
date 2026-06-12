package com.bookstore.bookstore.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google")
public record GoogleAuthProperties(
        String clientId
) {
}
