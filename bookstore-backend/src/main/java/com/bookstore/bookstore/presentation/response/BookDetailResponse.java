package com.bookstore.bookstore.presentation.response;

import java.util.UUID;

public record BookDetailResponse(
        UUID id,
        UUID bookId,
        Integer pageCount,
        Integer publicationYear,
        String language,
        String coverType,
        String dimensions,
        Integer weight,
        String translator,
        String edition
) {
}
