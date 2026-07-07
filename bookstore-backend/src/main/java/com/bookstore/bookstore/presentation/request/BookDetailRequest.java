package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record BookDetailRequest(
        UUID id,
        @Positive(message = "pageCount phải lớn hơn 0")
        Integer pageCount,
        @Positive(message = "publicationYear phải lớn hơn 0")
        Integer publicationYear,
        String language,
        String coverType,
        String dimensions,
        @Positive(message = "weight phải lớn hơn 0")
        Integer weight,
        String translator,
        String edition
) {
}

