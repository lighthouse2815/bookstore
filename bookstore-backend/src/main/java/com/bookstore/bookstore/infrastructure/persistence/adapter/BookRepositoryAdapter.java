package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.result.LowStockBookResult;
import com.bookstore.bookstore.application.result.LowStockReportRowResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.BookPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.AuthorJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.CategoryJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.FileAssetJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.PublisherJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryAdapter implements IBookRepository {

    private final BookJpaRepository bookJpaRepository;
    private final BookPersistenceMapper bookPersistenceMapper;
    private final CategoryJpaRepository categoryJpaRepository;
    private final AuthorJpaRepository authorJpaRepository;
    private final PublisherJpaRepository publisherJpaRepository;
    private final FileAssetJpaRepository fileAssetJpaRepository;

    @Override
    public List<Book> findAllActive() {
        return bookJpaRepository.findAllByDeletedAtIsNull().stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<Book> findPageActive(int page, int size) {
        var resultPage = bookJpaRepository.findPageIdsByDeletedAtIsNull(PageRequest.of(page, size));
        return new PageSliceResult<>(
                loadActiveBooksInOrder(resultPage.getContent()),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public List<Book> findAllIncludingDeleted() {
        return bookJpaRepository.findAll().stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Book> findByIdActive(UUID bookId) {
        return bookJpaRepository.findByIdAndDeletedAtIsNull(bookId)
                .map(bookPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Book> findByIdIncludingDeleted(UUID bookId) {
        return bookJpaRepository.findById(bookId)
                .map(bookPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByIdIncludingDeleted(UUID bookId) {
        return bookJpaRepository.existsById(bookId);
    }

    @Override
    public List<Book> findAllByIdsActive(Collection<UUID> bookIds) {
        return loadActiveBooksInOrder(bookIds);
    }

    @Override
    public List<Book> findAllByIdsIncludingDeleted(Collection<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return List.of();
        }

        return bookJpaRepository.findAllByIdIn(bookIds).stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Book> findAllByIdsIncludingDeletedForUpdate(Collection<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return List.of();
        }

        List<UUID> sortedBookIds = bookIds.stream()
                .distinct()
                .sorted()
                .toList();

        return bookJpaRepository.findAllByIdInForUpdate(sortedBookIds).stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Book> findRelatedActiveByCategoryId(UUID categoryId, UUID excludedBookId, int limit) {
        if (categoryId == null || excludedBookId == null || limit <= 0) {
            return List.of();
        }

        return loadActiveBooksInOrder(
                bookJpaRepository.findRelatedActiveIdsByCategoryId(categoryId, excludedBookId, PageRequest.of(0, limit))
        );
    }

    @Override
    public List<Book> searchByKeywordActive(String keyword) {
        return bookJpaRepository.searchByKeywordActive(keyword).stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<Book> searchPageByKeywordActive(String keyword, int page, int size) {
        var resultPage = bookJpaRepository.searchPageIdsByKeywordActive(keyword, PageRequest.of(page, size));
        return new PageSliceResult<>(
                loadActiveBooksInOrder(resultPage.getContent()),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public PageSliceResult<Book> searchPageActive(String keyword, UUID categoryId, int page, int size) {
        var resultPage = bookJpaRepository.searchPageIdsActive(keyword, categoryId, PageRequest.of(page, size));
        return new PageSliceResult<>(
                loadActiveBooksInOrder(resultPage.getContent()),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public long countActiveBooks() {
        return bookJpaRepository.countByDeletedAtIsNull();
    }

    @Override
    public long countLowStockBooks(int threshold) {
        return bookJpaRepository.countByDeletedAtIsNullAndStockQuantityLessThanEqual(threshold);
    }

    @Override
    public List<LowStockBookResult> findLowStockBooks(int threshold) {
        return bookJpaRepository.findLowStockBooks(threshold).stream()
                .map(row -> new LowStockBookResult(
                        row.getBookId(),
                        row.getTitle(),
                        row.getStockQuantity() == null ? 0 : row.getStockQuantity()
                ))
                .toList();
    }

    @Override
    public List<LowStockReportRowResult> findLowStockReportRows(int threshold) {
        return bookJpaRepository.findLowStockReportRows(threshold).stream()
                .map(row -> new LowStockReportRowResult(
                        row.getBookId(),
                        row.getTitle(),
                        row.getIsbn(),
                        row.getStockQuantity() == null ? 0 : row.getStockQuantity(),
                        row.getCategoryName()
                ))
                .toList();
    }

    @Override
    public Book save(Book book) {
        BookJpaEntity entity = bookJpaRepository.findById(book.getId())
                .orElseGet(BookJpaEntity::new);

        var category = categoryJpaRepository.getReferenceById(book.getCategoryId());
        var author = authorJpaRepository.getReferenceById(book.getAuthorId());
        var publisher = publisherJpaRepository.getReferenceById(book.getPublisherId());
        var fileAssetsById = book.getImages().stream()
                .map(image -> image.getFileAssetId())
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        fileAssetJpaRepository::getReferenceById
                ));

        bookPersistenceMapper.copyToEntity(entity, book, category, author, publisher, fileAssetsById);
        return bookPersistenceMapper.toDomain(bookJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID bookId) {
        bookJpaRepository.deleteById(bookId);
    }

    private List<Book> loadActiveBooksInOrder(Collection<UUID> bookIds) {
        List<UUID> orderedIds = bookIds == null
                ? List.of()
                : bookIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
        if (orderedIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, BookJpaEntity> booksById = bookJpaRepository.findAllByDeletedAtIsNullAndIdIn(orderedIds).stream()
                .collect(Collectors.toMap(BookJpaEntity::getId, Function.identity()));

        return orderedIds.stream()
                .map(booksById::get)
                .filter(Objects::nonNull)
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }
}
