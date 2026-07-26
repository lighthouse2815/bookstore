package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IImportReceiptRepository;
import com.bookstore.bookstore.domain.model.ImportReceipt;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.infrastructure.persistence.entity.ImportReceiptJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.SupplierJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ImportReceiptPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.ImportReceiptJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.SupplierJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.PageRequest;

@Repository
@RequiredArgsConstructor
public class ImportReceiptRepositoryAdapter implements IImportReceiptRepository {

    private final ImportReceiptJpaRepository importReceiptJpaRepository;
    private final SupplierJpaRepository supplierJpaRepository;
    private final ImportReceiptPersistenceMapper importReceiptPersistenceMapper;

    @Override
    public List<ImportReceipt> findAll() {
        return importReceiptJpaRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(importReceiptPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<ImportReceipt> findPage(int page, int size) {
        var result = importReceiptJpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return new PageSliceResult<>(
                result.getContent().stream().map(importReceiptPersistenceMapper::toDomain).toList(),
                result.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public Optional<ImportReceipt> findById(UUID receiptId) {
        return importReceiptJpaRepository.findById(receiptId)
                .map(importReceiptPersistenceMapper::toDomain);
    }

    @Override
    public ImportReceipt save(ImportReceipt importReceipt) {
        ImportReceiptJpaEntity entity = importReceiptJpaRepository.findById(importReceipt.getId())
                .orElseGet(ImportReceiptJpaEntity::new);

        SupplierJpaEntity supplier = supplierJpaRepository.getReferenceById(importReceipt.getSupplierId());
        importReceiptPersistenceMapper.copyToEntity(importReceipt, entity, supplier);
        return importReceiptPersistenceMapper.toDomain(importReceiptJpaRepository.save(entity));
    }
}
