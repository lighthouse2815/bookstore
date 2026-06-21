package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class DigitalAssetPersistenceMapper {

    public DigitalAsset toDomain(DigitalAssetJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new DigitalAsset(
                entity.getId(),
                entity.getBook().getId(),
                entity.getFormat(),
                entity.getTitle(),
                entity.getFileName(),
                entity.getStorageKey(),
                entity.getMimeType(),
                entity.getFileSize(),
                entity.getChecksum(),
                entity.getSampleStorageKey(),
                entity.getPrice(),
                entity.isDownloadAllowed(),
                entity.isPublished(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(DigitalAsset digitalAsset, DigitalAssetJpaEntity entity, BookJpaEntity book) {
        entity.setId(digitalAsset.getId());
        entity.setBook(book);
        entity.setFormat(digitalAsset.getFormat());
        entity.setTitle(digitalAsset.getTitle());
        entity.setFileName(digitalAsset.getFileName());
        entity.setStorageKey(digitalAsset.getStorageKey());
        entity.setMimeType(digitalAsset.getMimeType());
        entity.setFileSize(digitalAsset.getFileSize());
        entity.setChecksum(digitalAsset.getChecksum());
        entity.setSampleStorageKey(digitalAsset.getSampleStorageKey());
        entity.setPrice(digitalAsset.getPrice());
        entity.setDownloadAllowed(digitalAsset.isDownloadAllowed());
        entity.setPublished(digitalAsset.isPublished());
        entity.setCreatedAt(digitalAsset.getCreatedAt());
        entity.setUpdatedAt(digitalAsset.getUpdatedAt());
        entity.setDeletedAt(digitalAsset.getDeletedAt());
    }
}
