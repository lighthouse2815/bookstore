package com.bookstore.bookstore.infrastructure.payment;

import com.bookstore.bookstore.application.port.out.ISepaySettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.sepay")
public record SepayProperties(
        String merchantId,
        String secretKey,
        String webhookApiKey
) implements ISepaySettings {
}
