package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.PublisherJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublisherJpaRepository extends JpaRepository<PublisherJpaEntity, UUID> {

    List<PublisherJpaEntity> findAllByDeletedAtIsNull();

    Page<PublisherJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    Optional<PublisherJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<PublisherJpaEntity> findById(UUID id);

    boolean existsById(UUID id);

    Optional<PublisherJpaEntity> findByNameAndDeletedAtIsNull(String name);

    boolean existsByName(String name);
}
