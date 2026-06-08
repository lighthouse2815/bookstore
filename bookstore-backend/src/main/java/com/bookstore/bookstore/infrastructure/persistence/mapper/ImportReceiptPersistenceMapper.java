package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.ImportReceipt;
import com.bookstore.bookstore.domain.model.ImportReceiptItem;
import com.bookstore.bookstore.infrastructure.persistence.entity.ImportReceiptItemJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ImportReceiptJpaEntity;
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
                entity.getSupplierId(),
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

    public void copyToEntity(ImportReceipt importReceipt, ImportReceiptJpaEntity entity) {
        entity.setId(importReceipt.getId());
        entity.setSupplierId(importReceipt.getSupplierId());
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
                    copyItemToEntity(item, itemEntity, entity);
                    return itemEntity;
                })
                .toList();

        entity.getItems().clear();
        entity.getItems().addAll(mappedItems);
    }

    private ImportReceiptItem toDomain(ImportReceiptItemJpaEntity entity) {
        return new ImportReceiptItem(
                entity.getId(),
                entity.getBookId(),
                entity.getBookTitle(),
                entity.getUnitCost(),
                entity.getQuantity(),
                entity.getLineTotal()
        );
    }

    private void copyItemToEntity(
            ImportReceiptItem item,
            ImportReceiptItemJpaEntity entity,
            ImportReceiptJpaEntity importReceiptEntity
    ) {
        entity.setId(item.getId());
        entity.setImportReceipt(importReceiptEntity);
        entity.setBookId(item.getBookId());
        entity.setBookTitle(item.getBookTitle());
        entity.setUnitCost(item.getUnitCost());
        entity.setQuantity(item.getQuantity());
        entity.setLineTotal(item.getLineTotal());
    }
}
