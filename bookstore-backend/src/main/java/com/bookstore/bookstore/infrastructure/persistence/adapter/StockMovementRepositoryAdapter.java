package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.infrastructure.persistence.entity.StockMovementJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.StockMovementPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.StockMovementJpaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockMovementRepositoryAdapter implements IStockMovementRepository {

    private final StockMovementJpaRepository stockMovementJpaRepository;
    private final StockMovementPersistenceMapper stockMovementPersistenceMapper;

    @Override
    public List<StockMovement> findAll() {
        return stockMovementJpaRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(stockMovementPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<StockMovement> findAllByBookId(UUID bookId) {
        return stockMovementJpaRepository.findAllByBookIdOrderByCreatedAtDesc(bookId).stream()
                .map(stockMovementPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public StockMovement save(StockMovement stockMovement) {
        StockMovementJpaEntity entity = stockMovementJpaRepository.findById(stockMovement.getId())
                .orElseGet(StockMovementJpaEntity::new);
        stockMovementPersistenceMapper.copyToEntity(stockMovement, entity);
        return stockMovementPersistenceMapper.toDomain(stockMovementJpaRepository.save(entity));
    }
}
