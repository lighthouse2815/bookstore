package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.infrastructure.persistence.entity.FileAssetJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class FileAssetPersistenceMapper {

    public FileAsset toDomain(FileAssetJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new FileAsset(
                entity.getId(),
                entity.getProvider(),
                entity.getPurpose(),
                entity.getBucket(),
                entity.getStorageKey(),
                entity.getPublicUrl(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getSizeBytes(),
                entity.getChecksumSha256(),
                entity.getVisibility(),
                entity.getStatus(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(FileAsset fileAsset, FileAssetJpaEntity entity) {
        entity.setId(fileAsset.getId());
        entity.setProvider(fileAsset.getProvider());
        entity.setPurpose(fileAsset.getPurpose());
        entity.setBucket(fileAsset.getBucket());
        entity.setStorageKey(fileAsset.getStorageKey());
        entity.setPublicUrl(fileAsset.getPublicUrl());
        entity.setOriginalName(fileAsset.getOriginalName());
        entity.setContentType(fileAsset.getContentType());
        entity.setSizeBytes(fileAsset.getSizeBytes());
        entity.setChecksumSha256(fileAsset.getChecksumSha256());
        entity.setVisibility(fileAsset.getVisibility());
        entity.setStatus(fileAsset.getStatus());
        entity.setCreatedBy(fileAsset.getCreatedBy());
        entity.setCreatedAt(fileAsset.getCreatedAt());
        entity.setUpdatedAt(fileAsset.getUpdatedAt());
        entity.setDeletedAt(fileAsset.getDeletedAt());
    }
}
