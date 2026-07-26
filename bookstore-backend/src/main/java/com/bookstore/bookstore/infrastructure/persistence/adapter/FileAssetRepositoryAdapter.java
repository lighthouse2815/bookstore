package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IFileAssetRepository;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.infrastructure.persistence.entity.FileAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.FileAssetPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.FileAssetJpaRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FileAssetRepositoryAdapter implements IFileAssetRepository {

    private final FileAssetJpaRepository fileAssetJpaRepository;
    private final FileAssetPersistenceMapper fileAssetPersistenceMapper;

    @Override
    public Optional<FileAsset> findByIdActive(UUID fileAssetId) {
        return fileAssetJpaRepository.findByIdAndStatusAndDeletedAtIsNull(fileAssetId, FileStatus.ACTIVE)
                .map(fileAssetPersistenceMapper::toDomain);
    }

    @Override
    public Optional<FileAsset> findByIdIncludingDeleted(UUID fileAssetId) {
        return fileAssetJpaRepository.findById(fileAssetId)
                .map(fileAssetPersistenceMapper::toDomain);
    }

    @Override
    public List<FileAsset> findAllByIdsActive(Collection<UUID> fileAssetIds) {
        if (fileAssetIds == null || fileAssetIds.isEmpty()) {
            return List.of();
        }

        return fileAssetJpaRepository.findAllByIdInAndStatusAndDeletedAtIsNull(fileAssetIds, FileStatus.ACTIVE)
                .stream()
                .map(fileAssetPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<String> findUsageReferences(UUID fileAssetId) {
        if (fileAssetId == null) {
            return List.of();
        }

        List<String> usageReferences = new ArrayList<>();
        addUsageReferenceIfPresent(
                usageReferences,
                "book_images.file_asset_id",
                fileAssetJpaRepository.countBookImageUsages(fileAssetId)
        );
        addUsageReferenceIfPresent(
                usageReferences,
                "profiles.avatar_file_asset_id",
                fileAssetJpaRepository.countProfileAvatarUsages(fileAssetId)
        );
        addUsageReferenceIfPresent(
                usageReferences,
                "authors.avatar_file_asset_id",
                fileAssetJpaRepository.countAuthorAvatarUsages(fileAssetId)
        );
        addUsageReferenceIfPresent(
                usageReferences,
                "categories.image_file_asset_id",
                fileAssetJpaRepository.countCategoryImageUsages(fileAssetId)
        );
        addUsageReferenceIfPresent(
                usageReferences,
                "publishers.logo_file_asset_id",
                fileAssetJpaRepository.countPublisherLogoUsages(fileAssetId)
        );
        addUsageReferenceIfPresent(
                usageReferences,
                "digital_assets.file_asset_id",
                fileAssetJpaRepository.countDigitalAssetMainUsages(fileAssetId)
        );
        addUsageReferenceIfPresent(
                usageReferences,
                "digital_assets.sample_file_asset_id",
                fileAssetJpaRepository.countDigitalAssetSampleUsages(fileAssetId)
        );
        return List.copyOf(usageReferences);
    }

    @Override
    public long calculateReservedStorageBytes() {
        return fileAssetJpaRepository.calculateReservedStorageBytes(List.of(FileStatus.PENDING, FileStatus.ACTIVE));
    }

    @Override
    public long countUploadsCreatedAtOrAfter(Instant createdAt) {
        return fileAssetJpaRepository.countByCreatedAtGreaterThanEqual(createdAt);
    }

    @Override
    public FileAsset save(FileAsset fileAsset) {
        FileAssetJpaEntity entity = fileAssetJpaRepository.findById(fileAsset.getId())
                .orElseGet(FileAssetJpaEntity::new);
        fileAssetPersistenceMapper.copyToEntity(fileAsset, entity);
        return fileAssetPersistenceMapper.toDomain(fileAssetJpaRepository.save(entity));
    }

    private void addUsageReferenceIfPresent(List<String> usageReferences, String reference, long usageCount) {
        if (usageCount > 0) {
            usageReferences.add(reference);
        }
    }
}
