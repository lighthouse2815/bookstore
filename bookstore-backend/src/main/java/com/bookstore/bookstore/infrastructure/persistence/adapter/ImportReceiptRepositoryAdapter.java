package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IImportReceiptRepository;
import com.bookstore.bookstore.domain.model.ImportReceipt;
import com.bookstore.bookstore.infrastructure.persistence.entity.ImportReceiptJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ImportReceiptPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.ImportReceiptJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ImportReceiptRepositoryAdapter implements IImportReceiptRepository {

    private final ImportReceiptJpaRepository importReceiptJpaRepository;
    private final ImportReceiptPersistenceMapper importReceiptPersistenceMapper;

    @Override
    public List<ImportReceipt> findAll() {
        return importReceiptJpaRepository.findAllDetailed().stream()
                .map(importReceiptPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ImportReceipt> findById(UUID receiptId) {
        return importReceiptJpaRepository.findDetailedById(receiptId)
                .map(importReceiptPersistenceMapper::toDomain);
    }

    @Override
    public ImportReceipt save(ImportReceipt importReceipt) {
        ImportReceiptJpaEntity entity = importReceiptJpaRepository.findDetailedById(importReceipt.getId())
                .orElseGet(ImportReceiptJpaEntity::new);
        importReceiptPersistenceMapper.copyToEntity(importReceipt, entity);
        return importReceiptPersistenceMapper.toDomain(importReceiptJpaRepository.save(entity));
    }
}
