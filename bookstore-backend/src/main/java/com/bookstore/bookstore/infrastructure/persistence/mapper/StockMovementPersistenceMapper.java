package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.StockMovementJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class StockMovementPersistenceMapper {

    public StockMovement toDomain(StockMovementJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new StockMovement(
                entity.getId(),
                entity.getBook().getId(),
                entity.getType(),
                entity.getQuantity(),
                entity.getBeforeQuantity(),
                entity.getAfterQuantity(),
                entity.getReferenceId(),
                entity.getReferenceType(),
                entity.getNote(),
                entity.getCreatedAt(),
                entity.getCreatedBy().getId()
        );
    }

    public void copyToEntity(StockMovement stockMovement, StockMovementJpaEntity entity, BookJpaEntity book, UserJpaEntity createdBy) {
        entity.setId(stockMovement.getId());
        entity.setBook(book);
        entity.setType(stockMovement.getType());
        entity.setQuantity(stockMovement.getQuantity());
        entity.setBeforeQuantity(stockMovement.getBeforeQuantity());
        entity.setAfterQuantity(stockMovement.getAfterQuantity());
        entity.setReferenceId(stockMovement.getReferenceId());
        entity.setReferenceType(stockMovement.getReferenceType());
        entity.setNote(stockMovement.getNote());
        entity.setCreatedAt(stockMovement.getCreatedAt());
        entity.setCreatedBy(createdBy);
    }
}
