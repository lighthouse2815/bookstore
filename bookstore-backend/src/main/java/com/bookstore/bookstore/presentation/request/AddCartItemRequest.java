package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddCartItemRequest(
        @NotNull(message = "bookId không được null")
        UUID bookId,

        @Min(value = 1, message = "quantity phải lớn hơn 0")
        int quantity
) {
}

