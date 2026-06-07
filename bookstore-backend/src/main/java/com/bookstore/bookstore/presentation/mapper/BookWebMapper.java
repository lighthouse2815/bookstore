package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateBookCommand;
import com.bookstore.bookstore.application.command.DeleteBookCommand;
import com.bookstore.bookstore.application.command.UpdateBookCommand;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.presentation.request.CreateBookRequest;
import com.bookstore.bookstore.presentation.request.UpdateBookRequest;
import com.bookstore.bookstore.presentation.response.BookResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BookWebMapper {

    public CreateBookCommand toCreateCommand(CreateBookRequest request) {
        return new CreateBookCommand(
                request.title(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.imageUrl(),
                request.categoryId(),
                request.authorId(),
                request.publisherId()
        );
    }

    public UpdateBookCommand toUpdateCommand(UUID bookId, UpdateBookRequest request) {
        return new UpdateBookCommand(
                bookId,
                request.title(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                request.imageUrl(),
                request.categoryId(),
                request.authorId(),
                request.publisherId()
        );
    }

    public DeleteBookCommand toDeleteCommand(UUID bookId) {
        return new DeleteBookCommand(bookId);
    }

    public BookResponse toBookResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getDescription(),
                book.getPrice(),
                book.getStockQuantity(),
                book.getImageUrl(),
                book.getCategoryId(),
                book.getAuthorId(),
                book.getPublisherId(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}
