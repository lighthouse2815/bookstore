package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BookPersistenceMapper {

    public Book toDomain(BookJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Book(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStockQuantity(),
                entity.getImageUrl(),
                entity.getCategoryId(),
                entity.getAuthorId(),
                entity.getPublisherId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public BookJpaEntity toEntity(Book book) {
        BookJpaEntity entity = new BookJpaEntity();
        copyToEntity(entity, book);
        return entity;
    }

    public void copyToEntity(BookJpaEntity entity, Book book) {
        entity.setId(book.getId());
        entity.setTitle(book.getTitle());
        entity.setDescription(book.getDescription());
        entity.setPrice(book.getPrice());
        entity.setStockQuantity(book.getStockQuantity());
        entity.setImageUrl(book.getImageUrl());
        entity.setCategoryId(book.getCategoryId());
        entity.setAuthorId(book.getAuthorId());
        entity.setPublisherId(book.getPublisherId());
        entity.setCreatedAt(book.getCreatedAt());
        entity.setUpdatedAt(book.getUpdatedAt());
        entity.setDeletedAt(book.getDeletedAt());
    }
}
