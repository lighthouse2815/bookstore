package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(
        @Min(value = 1, message = "quantity phải lớn hơn 0")
        int quantity
) {
}

