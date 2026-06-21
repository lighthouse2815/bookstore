package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.SupplierJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, UUID> {

    List<SupplierJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<SupplierJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<SupplierJpaEntity> findById(UUID id);

    boolean existsByName(String name);
}
