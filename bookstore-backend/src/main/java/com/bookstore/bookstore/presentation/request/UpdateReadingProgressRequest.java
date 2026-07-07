package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateReadingProgressRequest(
        @Min(value = 0, message = "currentPage không được âm")
        Integer currentPage,

        @NotNull(message = "progressPercent không được null")
        @DecimalMin(value = "0.0", message = "progressPercent không được âm")
        @DecimalMax(value = "100.0", message = "progressPercent không được lớn hơn 100")
        BigDecimal progressPercent,

        String positionData
) {
}

