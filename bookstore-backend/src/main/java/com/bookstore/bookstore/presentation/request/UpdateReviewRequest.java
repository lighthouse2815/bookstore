package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(
        @Min(value = 1, message = "rating phải tu 1 den 5")
        @Max(value = 5, message = "rating phải tu 1 den 5")
        int rating,
        @Size(max = 1000, message = "comment không được vượt quá 1000 ký tự")
        String comment
) {
}

