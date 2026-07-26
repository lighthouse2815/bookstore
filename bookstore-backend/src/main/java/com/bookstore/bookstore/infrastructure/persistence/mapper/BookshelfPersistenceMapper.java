package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Bookshelf;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookshelfJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BookshelfPersistenceMapper {

    public Bookshelf toDomain(BookshelfJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Bookshelf(
                entity.getId(),
                entity.getUser().getId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(Bookshelf bookshelf, BookshelfJpaEntity entity, UserJpaEntity user) {
        entity.setId(bookshelf.getId());
        entity.setUser(user);
        entity.setName(bookshelf.getName());
        entity.setCreatedAt(bookshelf.getCreatedAt());
        entity.setUpdatedAt(bookshelf.getUpdatedAt());
        entity.setDeletedAt(bookshelf.getDeletedAt());
    }
}
