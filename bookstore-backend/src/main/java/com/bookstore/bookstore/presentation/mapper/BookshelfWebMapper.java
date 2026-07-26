package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.AddBookshelfItemCommand;
import com.bookstore.bookstore.application.command.CreateBookshelfCommand;
import com.bookstore.bookstore.application.command.DeleteBookshelfCommand;
import com.bookstore.bookstore.application.command.RemoveBookshelfItemCommand;
import com.bookstore.bookstore.application.command.ReorderBookshelfItemsCommand;
import com.bookstore.bookstore.application.command.UpdateBookshelfCommand;
import com.bookstore.bookstore.application.result.BookshelfItemResult;
import com.bookstore.bookstore.application.result.BookshelfResult;
import com.bookstore.bookstore.application.result.BookshelfSummaryResult;
import com.bookstore.bookstore.presentation.request.CreateBookshelfRequest;
import com.bookstore.bookstore.presentation.request.ReorderBookshelfItemsRequest;
import com.bookstore.bookstore.presentation.request.UpdateBookshelfRequest;
import com.bookstore.bookstore.presentation.response.BookshelfItemResponse;
import com.bookstore.bookstore.presentation.response.BookshelfResponse;
import com.bookstore.bookstore.presentation.response.BookshelfSummaryResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class BookshelfWebMapper {

    private final BookWebMapper bookWebMapper;

    public BookshelfWebMapper(BookWebMapper bookWebMapper) {
        this.bookWebMapper = bookWebMapper;
    }

    public CreateBookshelfCommand toCreateCommand(UUID userId, CreateBookshelfRequest request) {
        return new CreateBookshelfCommand(userId, request.name());
    }

    public UpdateBookshelfCommand toUpdateCommand(UUID shelfId, UUID userId, UpdateBookshelfRequest request) {
        return new UpdateBookshelfCommand(shelfId, userId, request.name());
    }

    public DeleteBookshelfCommand toDeleteCommand(UUID shelfId, UUID userId) {
        return new DeleteBookshelfCommand(shelfId, userId);
    }

    public AddBookshelfItemCommand toAddItemCommand(UUID shelfId, UUID userId, UUID bookId) {
        return new AddBookshelfItemCommand(shelfId, userId, bookId);
    }

    public RemoveBookshelfItemCommand toRemoveItemCommand(UUID shelfId, UUID userId, UUID bookId) {
        return new RemoveBookshelfItemCommand(shelfId, userId, bookId);
    }

    public ReorderBookshelfItemsCommand toReorderCommand(
            UUID shelfId,
            UUID userId,
            ReorderBookshelfItemsRequest request
    ) {
        return new ReorderBookshelfItemsCommand(shelfId, userId, request.itemIds());
    }

    public BookshelfSummaryResponse toSummaryResponse(BookshelfSummaryResult result) {
        return new BookshelfSummaryResponse(
                result.id(),
                result.name(),
                result.bookCount(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    public BookshelfResponse toResponse(BookshelfResult result) {
        return new BookshelfResponse(
                result.id(),
                result.name(),
                result.bookCount(),
                result.items().stream().map(this::toItemResponse).toList(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private BookshelfItemResponse toItemResponse(BookshelfItemResult result) {
        return new BookshelfItemResponse(
                result.id(),
                result.sortOrder(),
                result.createdAt(),
                result.updatedAt(),
                bookWebMapper.toBookResponse(result.book())
        );
    }
}
