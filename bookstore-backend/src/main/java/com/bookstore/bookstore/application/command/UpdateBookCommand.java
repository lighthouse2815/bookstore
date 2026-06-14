package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateBookCommand(
        UUID bookId,
        String title,
        String isbn,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        List<BookImageCommand> images,
        BookDetailCommand detail,
        UUID categoryId,
        UUID authorId,
        UUID publisherId
) {
    public UpdateBookCommand {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }
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
