package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.FileAssetJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DigitalAssetPersistenceMapper {

    private final FileAssetPersistenceMapper fileAssetPersistenceMapper;

    public DigitalAsset toDomain(DigitalAssetJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        if (entity.getFileAsset() == null) {
            return null;
        }

        return new DigitalAsset(
                entity.getId(),
                entity.getBook().getId(),
                entity.getFormat(),
                entity.getTitle(),
                fileAssetPersistenceMapper.toDomain(entity.getFileAsset()),
                fileAssetPersistenceMapper.toDomain(entity.getSampleFileAsset()),
                entity.getPrice(),
                entity.isDownloadAllowed(),
                entity.getPurchaseAllowed() == null || entity.getPurchaseAllowed(),
                entity.isPublished(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            DigitalAsset digitalAsset,
            DigitalAssetJpaEntity entity,
            BookJpaEntity book,
            FileAssetJpaEntity fileAsset,
            FileAssetJpaEntity sampleFileAsset
    ) {
        entity.setId(digitalAsset.getId());
        entity.setBook(book);
        entity.setFormat(digitalAsset.getFormat());
        entity.setTitle(digitalAsset.getTitle());
        entity.setFileAsset(fileAsset);
        entity.setSampleFileAsset(sampleFileAsset);
        entity.setFileName(null);
        entity.setStorageKey(null);
        entity.setMimeType(null);
        entity.setFileSize(null);
        entity.setChecksum(null);
        entity.setSampleStorageKey(null);
        entity.setPrice(digitalAsset.getPrice());
        entity.setDownloadAllowed(digitalAsset.isDownloadAllowed());
        entity.setPurchaseAllowed(digitalAsset.isPurchaseAllowed());
        entity.setPublished(digitalAsset.isPublished());
        entity.setCreatedAt(digitalAsset.getCreatedAt());
        entity.setUpdatedAt(digitalAsset.getUpdatedAt());
        entity.setDeletedAt(digitalAsset.getDeletedAt());
    }
}
