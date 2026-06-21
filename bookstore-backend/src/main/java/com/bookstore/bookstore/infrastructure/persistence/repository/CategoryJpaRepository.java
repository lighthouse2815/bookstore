package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    List<CategoryJpaEntity> findAllByDeletedAtIsNull();

    List<CategoryJpaEntity> findAll();

    Optional<CategoryJpaEntity> findByIdAndDeletedAtIsNull(@Param("id") UUID id);

    Optional<CategoryJpaEntity> findById(@Param("id") UUID id);

    boolean existsById(@Param("id") UUID id);

    Optional<CategoryJpaEntity> findByNameAndDeletedAtIsNull(@Param("name") String name);

    boolean existsByName(@Param("name") String name);


}


