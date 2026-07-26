package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateReviewRequest(
        @NotNull(message = "orderItemId không được null")
        UUID orderItemId,
        @Min(value = 1, message = "rating phải tu 1 den 5")
        @Max(value = 5, message = "rating phải tu 1 den 5")
        int rating,
        @Size(max = 1000, message = "comment không được vượt quá 1000 ký tự")
        String comment
) {
}

