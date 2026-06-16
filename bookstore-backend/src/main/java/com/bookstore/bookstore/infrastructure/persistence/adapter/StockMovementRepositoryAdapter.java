package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.infrastructure.persistence.entity.BookJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.StockMovementJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.StockMovementPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.BookJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.StockMovementJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockMovementRepositoryAdapter implements IStockMovementRepository {

    private final StockMovementJpaRepository stockMovementJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final UserJpaRepository userJpaRepository;
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
        
        BookJpaEntity book = bookJpaRepository.getReferenceById(stockMovement.getBookId());
        UserJpaEntity createdBy = userJpaRepository.getReferenceById(stockMovement.getCreatedBy());
        
        stockMovementPersistenceMapper.copyToEntity(stockMovement, entity, book, createdBy);
        return stockMovementPersistenceMapper.toDomain(stockMovementJpaRepository.save(entity));
    }
}
