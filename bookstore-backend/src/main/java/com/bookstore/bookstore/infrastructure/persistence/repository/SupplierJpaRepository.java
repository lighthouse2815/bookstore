package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.SupplierJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, UUID> {

    List<SupplierJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Page<SupplierJpaEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<SupplierJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<SupplierJpaEntity> findById(UUID id);

    boolean existsByName(String name);
}
