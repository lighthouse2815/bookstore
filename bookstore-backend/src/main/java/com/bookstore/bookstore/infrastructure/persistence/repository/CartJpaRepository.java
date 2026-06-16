package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.CartJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<CartJpaEntity, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<CartJpaEntity> findByUser_Id(UUID userId);

}

