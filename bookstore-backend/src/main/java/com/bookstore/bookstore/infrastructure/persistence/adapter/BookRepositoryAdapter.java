package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.BookPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.AuthorJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.CategoryJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.PublisherJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    @Override
    public List<Book> findAllActive() {
        return bookJpaRepository.findAllByDeletedAtIsNull().stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
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
    public List<Book> findAllByIdsIncludingDeleted(Collection<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return List.of();
        }

        return bookJpaRepository.findAllByIdIn(bookIds).stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Book> findRelatedActiveByCategoryId(UUID categoryId, UUID excludedBookId, int limit) {
        if (categoryId == null || excludedBookId == null || limit <= 0) {
            return List.of();
        }

        return bookJpaRepository.findRelatedActiveByCategoryId(categoryId, excludedBookId, PageRequest.of(0, limit)).stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Book> searchByKeywordActive(String keyword) {
        return bookJpaRepository.searchByKeywordActive(keyword).stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Book save(Book book) {
        BookJpaEntity entity = bookJpaRepository.findById(book.getId())
                .orElseGet(BookJpaEntity::new);

        var category = categoryJpaRepository.getReferenceById(book.getCategoryId());
        var author = authorJpaRepository.getReferenceById(book.getAuthorId());
        var publisher = publisherJpaRepository.getReferenceById(book.getPublisherId());

        bookPersistenceMapper.copyToEntity(entity, book, category, author, publisher);
        return bookPersistenceMapper.toDomain(bookJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID bookId) {
        bookJpaRepository.deleteById(bookId);
    }
}
