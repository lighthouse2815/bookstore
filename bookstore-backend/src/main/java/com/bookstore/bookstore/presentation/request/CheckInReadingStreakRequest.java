package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CheckInReadingStreakRequest(
        @NotNull(message = "bookId khong duoc null")
        UUID bookId,

        @Size(max = 2000, message = "note khong duoc vuot qua 2000 ky tu")
        String note,

        @PositiveOrZero(message = "currentPage khong duoc am")
        Integer currentPage,

        @DecimalMin(value = "0.0", message = "progressPercent phai tu 0 den 100")
        @DecimalMax(value = "100.0", message = "progressPercent phai tu 0 den 100")
        BigDecimal progressPercent
) {
}
