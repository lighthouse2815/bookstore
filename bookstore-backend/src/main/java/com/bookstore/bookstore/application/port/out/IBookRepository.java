package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.LowStockBookResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.Book;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IBookRepository {

    List<Book> findAllActive();

    PageSliceResult<Book> findPageActive(int page, int size);

    List<Book> findAllIncludingDeleted();

    Optional<Book> findByIdActive(UUID bookId);

    Optional<Book> findByIdIncludingDeleted(UUID bookId);

    boolean existsByIdIncludingDeleted(UUID bookId);

    List<Book> findAllByIdsActive(Collection<UUID> bookIds);

    List<Book> findAllByIdsIncludingDeleted(Collection<UUID> bookIds);

    List<Book> findAllByIdsIncludingDeletedForUpdate(Collection<UUID> bookIds);

    List<Book> findRelatedActiveByCategoryId(UUID categoryId, UUID excludedBookId, int limit);

    List<Book> searchByKeywordActive(String keyword);

    PageSliceResult<Book> searchPageByKeywordActive(String keyword, int page, int size);

    PageSliceResult<Book> searchPageActive(String keyword, UUID categoryId, int page, int size);

    long countActiveBooks();

    long countLowStockBooks(int threshold);

    List<LowStockBookResult> findLowStockBooks(int threshold);

    Book save(Book book);

    void deleteById(UUID bookId);
}
