package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.StockMovementJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementJpaRepository extends JpaRepository<StockMovementJpaEntity, UUID> {

    List<StockMovementJpaEntity> findAllByOrderByCreatedAtDesc();

    List<StockMovementJpaEntity> findAllByBookIdOrderByCreatedAtDesc(UUID bookId);
}
