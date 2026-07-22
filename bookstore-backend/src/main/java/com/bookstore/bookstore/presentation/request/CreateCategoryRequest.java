package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record CreateCategoryRequest(
        @NotBlank(message = "code không được để trống")
        @Pattern(regexp = "[A-Za-z0-9_]+", message = "code chỉ được chứa chữ cái, số và dấu gạch dưới")
        String code,
        @Valid
        @NotEmpty(message = "translations không được để trống")
        List<CategoryTranslationRequest> translations,
        java.util.UUID parentId,
        java.util.UUID imageFileAssetId
) {
}

