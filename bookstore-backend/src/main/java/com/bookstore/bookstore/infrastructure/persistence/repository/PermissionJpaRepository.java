package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.infrastructure.persistence.entity.PermissionJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, UUID> {

    List<PermissionJpaEntity> findAllByDeletedAtIsNull();

    Optional<PermissionJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<PermissionJpaEntity> findByCode(PermissionCode code);

    boolean existsById(UUID id);

    Optional<PermissionJpaEntity> findByCodeAndDeletedAtIsNull(PermissionCode code);

    boolean existsByCode(PermissionCode code);
}
