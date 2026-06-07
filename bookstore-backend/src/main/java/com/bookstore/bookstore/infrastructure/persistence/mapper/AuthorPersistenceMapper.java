package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.infrastructure.persistence.entity.AuthorJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthorPersistenceMapper {

    public Author toDomain(AuthorJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Author(
                entity.getId(),
                entity.getName(),
                entity.getBiography(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public AuthorJpaEntity toEntity(Author author) {
        AuthorJpaEntity entity = new AuthorJpaEntity();
        copyToEntity(entity, author);
        return entity;
    }

    public void copyToEntity(AuthorJpaEntity entity, Author author) {
        entity.setId(author.getId());
        entity.setName(author.getName());
        entity.setBiography(author.getBiography());
        entity.setCreatedAt(author.getCreatedAt());
        entity.setUpdatedAt(author.getUpdatedAt());
        entity.setDeletedAt(author.getDeletedAt());
    }
}
