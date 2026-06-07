package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateBookCommand(
        String title,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String imageUrl,
        UUID categoryId,
        UUID authorId,
        UUID publisherId
) {
    public CreateBookCommand {
        if (title == null || title.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "title");
        }
        if (price == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "price");
        }
        if (stockQuantity == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "stockQuantity");
        }
        if (categoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "categoryId");
        }
        if (authorId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "authorId");
        }
        if (publisherId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "publisherId");
        }
    }
}
