package com.bookstore.bookstore.application.command;

import java.util.UUID;

public record BookImageCommand(
        UUID id,
        String imageUrl,
        Boolean primaryImage,
        Integer sortOrder,
        String altText
) {
}
