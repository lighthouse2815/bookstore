package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record BookImageRequest(
        UUID id,
        @NotBlank(message = "imageUrl khong duoc de trong")
        String imageUrl,
        Boolean primaryImage,
        @Min(value = 0, message = "sortOrder khong duoc am")
        Integer sortOrder,
        String altText
) {
}
