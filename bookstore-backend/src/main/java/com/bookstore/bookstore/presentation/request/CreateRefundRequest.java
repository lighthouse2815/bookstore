package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.RefundMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateRefundRequest(
        UUID returnRequestId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @Size(min = 3, max = 3) String currency,
        @NotBlank @Size(max = 1000) String reason,
        @NotNull RefundMethod method
) { }
