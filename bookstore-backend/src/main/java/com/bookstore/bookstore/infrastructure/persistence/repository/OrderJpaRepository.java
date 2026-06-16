package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;


public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<OrderJpaEntity> findByIdAndUser_DeletedAtIsNull(UUID id);


    @EntityGraph(attributePaths = "items")
    List<OrderJpaEntity> findAllByUserIdAndUser_DeletedAtIsNull(UUID userId);


    @Query("""
            select i.book.id, sum(i.quantity)
            from OrderJpaEntity o
            join o.items i
            where o.status = com.bookstore.bookstore.domain.enums.OrderStatus.DELIVERED
              and i.book.id in :bookIds
              and i.book.deletedAt is null
            group by i.book.id
            """)
    List<Object[]> countDeliveredQuantityByBookIds(@Param("bookIds") Collection<UUID> bookIds);

    @EntityGraph(attributePaths = "items")
    List<OrderJpaEntity> findAllByUser_DeletedAtIsNull();


}

