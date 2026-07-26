package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BookImageRequest(
        UUID id,
        @NotNull(message = "fileAssetId không được null")
        UUID fileAssetId,
        Boolean primaryImage,
        @Min(value = 0, message = "sortOrder không được âm")
        Integer sortOrder,
        String altText
) {
}

