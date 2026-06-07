package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.BookPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryAdapter implements IBookRepository {

    private final BookJpaRepository bookJpaRepository;
    private final BookPersistenceMapper bookPersistenceMapper;

    @Override
    public List<Book> findAllActive() {
        return bookJpaRepository.findAllActive().stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Book> findAllIncludingDeleted() {
        return bookJpaRepository.findAllIncludingDeleted().stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Book> findByIdActive(UUID bookId) {
        return bookJpaRepository.findByIdActive(bookId)
                .map(bookPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Book> findByIdIncludingDeleted(UUID bookId) {
        return bookJpaRepository.findByIdIncludingDeleted(bookId)
                .map(bookPersistenceMapper::toDomain);
    }

    @Override
    public List<Book> searchByKeywordActive(String keyword) {
        return bookJpaRepository.searchByKeywordActive(keyword).stream()
                .map(bookPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Book save(Book book) {
        BookJpaEntity entity = bookJpaRepository.findByIdIncludingDeleted(book.getId())
                .orElseGet(BookJpaEntity::new);
        bookPersistenceMapper.copyToEntity(entity, book);
        return bookPersistenceMapper.toDomain(bookJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID bookId) {
        bookJpaRepository.deleteById(bookId);
    }
}
