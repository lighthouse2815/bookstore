package com.bookstore.bookstore.infrastructure.payment;

import com.bookstore.bookstore.application.port.out.IPaymentExpirySettings;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.payment")
public record PaymentExpiryProperties(
        @Min(5) @Max(1440) int bankTransferExpirationMinutes,
        boolean expiryJobEnabled,
        @Min(1_000) @Max(3_600_000) long expiryJobDelayMs,
        @Min(1) @Max(500) int expiryJobBatchSize
) implements IPaymentExpirySettings {
}
