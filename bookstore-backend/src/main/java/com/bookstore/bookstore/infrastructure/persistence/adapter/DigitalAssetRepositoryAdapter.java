package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.DigitalAssetPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.DigitalAssetJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.FileAssetJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DigitalAssetRepositoryAdapter implements IDigitalAssetRepository {

    private final DigitalAssetJpaRepository digitalAssetJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final FileAssetJpaRepository fileAssetJpaRepository;
    private final DigitalAssetPersistenceMapper digitalAssetPersistenceMapper;

    @Override
    public Optional<DigitalAsset> findByIdActive(UUID digitalAssetId) {
        return digitalAssetJpaRepository.findByIdAndDeletedAtIsNullAndBook_DeletedAtIsNull(digitalAssetId)
                .map(digitalAssetPersistenceMapper::toDomain);
    }

    @Override
    public Optional<DigitalAsset> findByIdIncludingDeleted(UUID digitalAssetId) {
        return digitalAssetJpaRepository.findById(digitalAssetId)
                .map(digitalAssetPersistenceMapper::toDomain);
    }

    @Override
    public List<DigitalAsset> findAllByBookIdActive(UUID bookId) {
        return digitalAssetJpaRepository
                .findAllByBook_IdAndDeletedAtIsNullAndBook_DeletedAtIsNullOrderByCreatedAtDesc(bookId)
                .stream()
                .map(digitalAssetPersistenceMapper::toDomain)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public List<DigitalAsset> findAllByBookIdIncludingDeleted(UUID bookId) {
        return digitalAssetJpaRepository.findAllByBook_IdOrderByCreatedAtDesc(bookId).stream()
                .map(digitalAssetPersistenceMapper::toDomain)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public List<DigitalAsset> findAllByBookIdsActive(List<UUID> bookIds) {
        return digitalAssetJpaRepository
                .findAllByBook_IdInAndDeletedAtIsNullAndBook_DeletedAtIsNullOrderByCreatedAtDesc(bookIds)
                .stream()
                .map(digitalAssetPersistenceMapper::toDomain)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public PageSliceResult<DigitalAsset> searchPublishedCatalog(
            String keyword,
            UUID categoryId,
            int page,
            int size
    ) {
        var resultPage = digitalAssetJpaRepository.searchPublishedCatalog(
                keyword,
                categoryId,
                PageRequest.of(page, size)
        );

        return new PageSliceResult<>(
                resultPage.stream()
                        .map(digitalAssetPersistenceMapper::toDomain)
                        .filter(java.util.Objects::nonNull)
                        .toList(),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public List<DigitalAsset> findAllByIdsActive(List<UUID> digitalAssetIds) {
        return digitalAssetJpaRepository.findAllByIdInAndDeletedAtIsNullAndBook_DeletedAtIsNull(digitalAssetIds).stream()
                .map(digitalAssetPersistenceMapper::toDomain)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public DigitalAsset save(DigitalAsset digitalAsset) {
        DigitalAssetJpaEntity entity = digitalAssetJpaRepository.findById(digitalAsset.getId())
                .orElseGet(DigitalAssetJpaEntity::new);
        BookJpaEntity book = bookJpaRepository.getReferenceById(digitalAsset.getBookId());
        var fileAsset = fileAssetJpaRepository.getReferenceById(digitalAsset.getFileAssetId());
        var sampleFileAsset = digitalAsset.getSampleFileAssetId() == null
                ? null
                : fileAssetJpaRepository.getReferenceById(digitalAsset.getSampleFileAssetId());

        digitalAssetPersistenceMapper.copyToEntity(digitalAsset, entity, book, fileAsset, sampleFileAsset);
        return digitalAssetPersistenceMapper.toDomain(digitalAssetJpaRepository.save(entity));
    }
}
