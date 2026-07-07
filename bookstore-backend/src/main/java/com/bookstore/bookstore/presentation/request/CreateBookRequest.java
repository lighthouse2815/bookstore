package com.bookstore.bookstore.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateBookRequest(
        @NotBlank(message = "title không được để trống")
        String title,
        String isbn,
        String description,
        @NotNull(message = "price không được null")
        @DecimalMin(value = "0.0", inclusive = true, message = "price không được âm")
        BigDecimal price,
        @NotNull(message = "stockQuantity không được null")
        @Min(value = 0, message = "stockQuantity không được âm")
        Integer stockQuantity,
        List<@Valid BookImageRequest> images,
        @Valid
        BookDetailRequest detail,
        @NotNull(message = "categoryId không được null")
        UUID categoryId,
        @NotNull(message = "authorId không được null")
        UUID authorId,
        @NotNull(message = "publisherId không được null")
        UUID publisherId
) {
}

