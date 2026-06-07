package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Publisher;
import com.bookstore.bookstore.infrastructure.persistence.entity.PublisherJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PublisherPersistenceMapper {

    public Publisher toDomain(PublisherJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Publisher(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
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

    public void copyToEntity(PublisherJpaEntity entity, Publisher publisher) {
        entity.setId(publisher.getId());
        entity.setName(publisher.getName());
        entity.setDescription(publisher.getDescription());
        entity.setCreatedAt(publisher.getCreatedAt());
        entity.setUpdatedAt(publisher.getUpdatedAt());
        entity.setDeletedAt(publisher.getDeletedAt());
    }
}
