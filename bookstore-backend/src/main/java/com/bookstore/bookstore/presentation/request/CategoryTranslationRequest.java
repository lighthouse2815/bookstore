package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoryTranslationRequest(
        @NotBlank(message = "locale không được để trống")
        @Pattern(regexp = "vi|en", message = "locale chỉ hỗ trợ vi hoặc en")
        String locale,
        @NotBlank(message = "name không được để trống")
        String name,
        String description
) {
}
