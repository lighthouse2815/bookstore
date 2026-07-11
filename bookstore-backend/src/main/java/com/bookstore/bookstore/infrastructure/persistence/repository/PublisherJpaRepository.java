package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.PublisherJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublisherJpaRepository extends JpaRepository<PublisherJpaEntity, UUID> {

    @EntityGraph(attributePaths = "logoFileAsset")
    List<PublisherJpaEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = "logoFileAsset")
    Page<PublisherJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "logoFileAsset")
    Optional<PublisherJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    @EntityGraph(attributePaths = "logoFileAsset")
    Optional<PublisherJpaEntity> findById(UUID id);

    boolean existsById(UUID id);

    @EntityGraph(attributePaths = "logoFileAsset")
    Optional<PublisherJpaEntity> findByNameAndDeletedAtIsNull(String name);

    boolean existsByName(String name);
}
