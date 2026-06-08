package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.CartJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartJpaRepository extends JpaRepository<CartJpaEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    @Query("""
            select c
            from CartJpaEntity c
            where c.userId = :userId
            """)
    Optional<CartJpaEntity> findByUserId(@Param("userId") UUID userId);
}
