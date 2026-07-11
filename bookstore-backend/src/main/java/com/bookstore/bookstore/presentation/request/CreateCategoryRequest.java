package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank(message = "name không được để trống")
        String name,
        String description,
        java.util.UUID parentId,
        java.util.UUID imageFileAssetId
) {
}

