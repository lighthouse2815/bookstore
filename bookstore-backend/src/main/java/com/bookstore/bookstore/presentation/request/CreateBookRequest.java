package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateBookRequest(
        @NotBlank(message = "title khong duoc de trong")
        String title,
        String description,
        @NotNull(message = "price khong duoc null")
        @DecimalMin(value = "0.0", inclusive = true, message = "price khong duoc am")
        BigDecimal price,
        @NotNull(message = "stockQuantity khong duoc null")
        @Min(value = 0, message = "stockQuantity khong duoc am")
        Integer stockQuantity,
        String imageUrl,
        @NotNull(message = "categoryId khong duoc null")
        UUID categoryId,
        @NotNull(message = "authorId khong duoc null")
        UUID authorId,
        @NotNull(message = "publisherId khong duoc null")
        UUID publisherId
) {
}
