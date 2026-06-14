package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Book;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IBookRepository {

    List<Book> findAllActive();

    List<Book> findAllIncludingDeleted();

    Optional<Book> findByIdActive(UUID bookId);

    Optional<Book> findByIdIncludingDeleted(UUID bookId);

    List<Book> findAllByIdsIncludingDeleted(Collection<UUID> bookIds);

    List<Book> findRelatedActiveByCategoryId(UUID categoryId, UUID excludedBookId, int limit);

    List<Book> searchByKeywordActive(String keyword);

    Book save(Book book);

    void deleteById(UUID bookId);
}
