package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.format.annotation.DateTimeFormat;

public record RevenueChartRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        @Pattern(
                regexp = "DAY|MONTH",
                message = "groupBy chỉ chấp nhận DAY hoặc MONTH"
        )
        String groupBy
) {

    public RevenueChartRequest {
        groupBy = groupBy == null || groupBy.isBlank()
                ? "DAY"
                : groupBy.trim().toUpperCase(Locale.ROOT);
    }

    @AssertTrue(message = "Ngày bắt đầu không được sau ngày kết thúc")
    public boolean isDateRangeValid() {
        return from == null || to == null || !from.isAfter(to);
    }
}
