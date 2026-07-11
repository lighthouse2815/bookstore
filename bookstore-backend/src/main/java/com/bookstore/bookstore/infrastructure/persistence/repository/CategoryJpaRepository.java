package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    @EntityGraph(attributePaths = "imageFileAsset")
    List<CategoryJpaEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = "imageFileAsset")
    Page<CategoryJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "imageFileAsset")
    List<CategoryJpaEntity> findAll();

    @EntityGraph(attributePaths = "imageFileAsset")
    Optional<CategoryJpaEntity> findByIdAndDeletedAtIsNull(@Param("id") UUID id);

    @EntityGraph(attributePaths = "imageFileAsset")
    Optional<CategoryJpaEntity> findById(@Param("id") UUID id);

    boolean existsById(@Param("id") UUID id);

    @EntityGraph(attributePaths = "imageFileAsset")
    Optional<CategoryJpaEntity> findByNameAndDeletedAtIsNull(@Param("name") String name);

    boolean existsByName(@Param("name") String name);


}
