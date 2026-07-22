package com.bookstore.bookstore.presentation.response;

public record CategoryTranslationResponse(
        String locale,
        String name,
        String description
) {
}
