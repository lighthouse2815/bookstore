package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.StockMovementJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockMovementJpaRepository extends JpaRepository<StockMovementJpaEntity, UUID> {

    List<StockMovementJpaEntity> findAllByOrderByCreatedAtDesc();

    Page<StockMovementJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<StockMovementJpaEntity> findAllByBookIdOrderByCreatedAtDesc(UUID bookId);
}
