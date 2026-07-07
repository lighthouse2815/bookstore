package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.infrastructure.persistence.entity.AuthorJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.FileAssetJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorPersistenceMapper {

    private final FileAssetPersistenceMapper fileAssetPersistenceMapper;

    public Author toDomain(AuthorJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Author(
                entity.getId(),
                entity.getName(),
                entity.getBiography(),
                fileAssetPersistenceMapper.toDomain(entity.getAvatarFileAsset()),
                entity.getBirthYear(),
                entity.getDeathYear(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public AuthorJpaEntity toEntity(Author author) {
        AuthorJpaEntity entity = new AuthorJpaEntity();
        copyToEntity(entity, author, null);
        return entity;
    }

    public void copyToEntity(AuthorJpaEntity entity, Author author, FileAssetJpaEntity avatarFileAsset) {
        entity.setId(author.getId());
        entity.setName(author.getName());
        entity.setBiography(author.getBiography());
        entity.setAvatarFileAsset(avatarFileAsset);
        entity.setAvatarUrl(null);
        entity.setBirthYear(author.getBirthYear());
        entity.setDeathYear(author.getDeathYear());
        entity.setCreatedAt(author.getCreatedAt());
        entity.setUpdatedAt(author.getUpdatedAt());
        entity.setDeletedAt(author.getDeletedAt());
    }
}
