package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Bookshelf;
import com.bookstore.bookstore.domain.model.BookshelfItem;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IBookshelfRepository {

    List<Bookshelf> findAllByUserIdActive(UUID userId);

    Optional<Bookshelf> findByIdAndUserIdActive(UUID shelfId, UUID userId);

    Optional<Bookshelf> findByUserIdAndName(UUID userId, String name);

    long countActiveItemsByShelfId(UUID shelfId);

    Map<UUID, Long> countActiveItemsByShelfIds(Collection<UUID> shelfIds);

    List<BookshelfItem> findAllItemsByShelfIdActive(UUID shelfId);

    Optional<BookshelfItem> findItemByShelfIdAndBookId(UUID shelfId, UUID bookId);

    Bookshelf save(Bookshelf bookshelf);

    BookshelfItem saveItem(BookshelfItem bookshelfItem);

    List<BookshelfItem> saveAllItems(List<BookshelfItem> bookshelfItems);
}
