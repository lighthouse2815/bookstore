package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.AddBookshelfItemCommand;
import com.bookstore.bookstore.application.command.CreateBookshelfCommand;
import com.bookstore.bookstore.application.command.DeleteBookshelfCommand;
import com.bookstore.bookstore.application.command.RemoveBookshelfItemCommand;
import com.bookstore.bookstore.application.command.ReorderBookshelfItemsCommand;
import com.bookstore.bookstore.application.command.UpdateBookshelfCommand;
import com.bookstore.bookstore.application.result.BookshelfResult;
import com.bookstore.bookstore.application.result.BookshelfSummaryResult;
import java.util.List;
import java.util.UUID;

public interface IBookshelfService {

    List<BookshelfSummaryResult> getMyBookshelves(UUID userId);

    BookshelfResult getMyBookshelf(UUID userId, UUID shelfId);

    BookshelfResult create(CreateBookshelfCommand command);

    BookshelfResult update(UpdateBookshelfCommand command);

    void delete(DeleteBookshelfCommand command);

    BookshelfResult addItem(AddBookshelfItemCommand command);

    BookshelfResult removeItem(RemoveBookshelfItemCommand command);

    BookshelfResult reorderItems(ReorderBookshelfItemsCommand command);
}
