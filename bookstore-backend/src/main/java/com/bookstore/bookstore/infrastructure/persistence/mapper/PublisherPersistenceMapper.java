package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Publisher;
import com.bookstore.bookstore.infrastructure.persistence.entity.FileAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.PublisherJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublisherPersistenceMapper {

    private final FileAssetPersistenceMapper fileAssetPersistenceMapper;

    public Publisher toDomain(PublisherJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Publisher(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                fileAssetPersistenceMapper.toDomain(entity.getLogoFileAsset()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public PublisherJpaEntity toEntity(Publisher publisher) {
        PublisherJpaEntity entity = new PublisherJpaEntity();
        copyToEntity(entity, publisher);
        return entity;
    }

    public void copyToEntity(PublisherJpaEntity entity, Publisher publisher, FileAssetJpaEntity logoFileAsset) {
        entity.setId(publisher.getId());
        entity.setName(publisher.getName());
        entity.setDescription(publisher.getDescription());
        entity.setLogoFileAsset(logoFileAsset);
        entity.setCreatedAt(publisher.getCreatedAt());
        entity.setUpdatedAt(publisher.getUpdatedAt());
        entity.setDeletedAt(publisher.getDeletedAt());
    }

    public void copyToEntity(PublisherJpaEntity entity, Publisher publisher) {
        copyToEntity(entity, publisher, null);
    }
}
