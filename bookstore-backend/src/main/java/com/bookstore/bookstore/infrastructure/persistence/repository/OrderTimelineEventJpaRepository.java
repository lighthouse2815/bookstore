package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.OrderTimelineEventJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTimelineEventJpaRepository extends JpaRepository<OrderTimelineEventJpaEntity, UUID> {

    @EntityGraph(attributePaths = "order")
    List<OrderTimelineEventJpaEntity> findAllByOrder_IdOrderByCreatedAtAscIdAsc(UUID orderId);
}
