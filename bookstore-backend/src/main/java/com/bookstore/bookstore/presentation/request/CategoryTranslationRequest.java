package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryTranslationRequest(
        @NotBlank(message = "locale không được để trống")
        String locale,
        @NotBlank(message = "name không được để trống")
        String name,
        String description
) {
}
