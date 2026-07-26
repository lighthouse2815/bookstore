package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.BookshelfItem;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookshelfItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookshelfJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BookshelfItemPersistenceMapper {

    public BookshelfItem toDomain(BookshelfItemJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new BookshelfItem(
                entity.getId(),
                entity.getShelf().getId(),
                entity.getBook().getId(),
                entity.getSortOrder(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            BookshelfItem bookshelfItem,
            BookshelfItemJpaEntity entity,
            BookshelfJpaEntity shelf,
            BookJpaEntity book
    ) {
        entity.setId(bookshelfItem.getId());
        entity.setShelf(shelf);
        entity.setBook(book);
        entity.setSortOrder(bookshelfItem.getSortOrder());
        entity.setCreatedAt(bookshelfItem.getCreatedAt());
        entity.setUpdatedAt(bookshelfItem.getUpdatedAt());
        entity.setDeletedAt(bookshelfItem.getDeletedAt());
    }
}
