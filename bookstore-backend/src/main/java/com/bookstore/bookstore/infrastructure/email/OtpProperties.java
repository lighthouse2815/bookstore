package com.bookstore.bookstore.infrastructure.email;

import com.bookstore.bookstore.application.port.out.IOtpSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(
        long expirationMinutes,
        long resendCooldownSeconds,
        long resendMaxRequestsPerWindow,
        long resendWindowMinutes
) implements IOtpSettings {
}
