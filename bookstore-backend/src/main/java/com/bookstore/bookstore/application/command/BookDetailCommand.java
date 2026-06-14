package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record BookDetailCommand(
        UUID id,
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
