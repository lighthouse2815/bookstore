package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record BookDetailRequest(
        UUID id,
        @Positive(message = "pageCount phai lon hon 0")
        Integer pageCount,
        @Positive(message = "publicationYear phai lon hon 0")
        Integer publicationYear,
        String language,
        String coverType,
        String dimensions,
        @Positive(message = "weight phai lon hon 0")
        Integer weight,
        String translator,
        String edition
) {
}
