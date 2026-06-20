package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateReadingProgressRequest(
        @Min(value = 0, message = "currentPage khong duoc am")
        Integer currentPage,

        @NotNull(message = "progressPercent khong duoc null")
        @DecimalMin(value = "0.0", message = "progressPercent khong duoc am")
        @DecimalMax(value = "100.0", message = "progressPercent khong duoc lon hon 100")
        BigDecimal progressPercent,

        String positionData
) {
}
