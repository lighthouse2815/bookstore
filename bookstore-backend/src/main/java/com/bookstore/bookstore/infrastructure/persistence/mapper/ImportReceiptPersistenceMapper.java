package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.ImportReceipt;
import com.bookstore.bookstore.domain.model.ImportReceiptItem;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ImportReceiptItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ImportReceiptJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.SupplierJpaEntity;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ImportReceiptPersistenceMapper {

    public ImportReceipt toDomain(ImportReceiptJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ImportReceipt(
                entity.getId(),
                entity.getSupplier().getId(),
                entity.getItems().stream()
                        .map(this::toDomain)
                        .toList(),
                entity.getTotalAmount(),
                entity.getNote(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy()
        );
    }

    public void copyToEntity(ImportReceipt importReceipt, ImportReceiptJpaEntity entity, SupplierJpaEntity supplier) {
        entity.setId(importReceipt.getId());
        entity.setSupplier(supplier);
        entity.setTotalAmount(importReceipt.getTotalAmount());
        entity.setNote(importReceipt.getNote());
        entity.setCreatedAt(importReceipt.getCreatedAt());
        entity.setUpdatedAt(importReceipt.getUpdatedAt());
        entity.setCreatedBy(importReceipt.getCreatedBy());

        Map<UUID, ImportReceiptItemJpaEntity> currentItems = entity.getItems().stream()
                .collect(Collectors.toMap(ImportReceiptItemJpaEntity::getId, Function.identity()));

        var mappedItems = importReceipt.getItems().stream()
                .map(item -> {
                    ImportReceiptItemJpaEntity itemEntity = currentItems.getOrDefault(
                            item.getId(),
                            new ImportReceiptItemJpaEntity()
                    );
                    // copyItemToEntity will be called by adapter with book reference
                    return itemEntity;
                })
                .toList();

        entity.getItems().clear();
        entity.getItems().addAll(mappedItems);
    }

    private ImportReceiptItem toDomain(ImportReceiptItemJpaEntity entity) {
        return new ImportReceiptItem(
                entity.getId(),
                entity.getBook().getId(),
                entity.getBookTitle(),
                entity.getUnitCost(),
                entity.getQuantity(),
                entity.getLineTotal()
        );
    }

    public void copyItemToEntity(
            ImportReceiptItem item,
            ImportReceiptItemJpaEntity entity,
            ImportReceiptJpaEntity importReceiptEntity,
            BookJpaEntity book
    ) {
        entity.setId(item.getId());
        entity.setImportReceipt(importReceiptEntity);
        entity.setBook(book);
        entity.setBookTitle(item.getBookTitle());
        entity.setUnitCost(item.getUnitCost());
        entity.setQuantity(item.getQuantity());
        entity.setLineTotal(item.getLineTotal());
    }
}
