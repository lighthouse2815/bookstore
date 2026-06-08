package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    @Query("""
            select o
            from OrderJpaEntity o
            where o.id = :orderId
            """)
    Optional<OrderJpaEntity> findDetailedById(@Param("orderId") UUID orderId);

    @EntityGraph(attributePaths = "items")
    @Query("""
            select o
            from OrderJpaEntity o
            where o.userId = :userId
            order by o.createdAt desc
            """)
    List<OrderJpaEntity> findAllByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = "items")
    @Query("""
            select o
            from OrderJpaEntity o
            order by o.createdAt desc
            """)
    List<OrderJpaEntity> findAllDetailed();
}
