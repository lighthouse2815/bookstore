package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.AuthorJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorJpaRepository extends JpaRepository<AuthorJpaEntity, UUID> {

    List<AuthorJpaEntity> findAllByDeletedAtIsNull();

    List<AuthorJpaEntity> findAll();

    Optional<AuthorJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<AuthorJpaEntity> findById(UUID id);

    boolean existsById(UUID id);

    Optional<AuthorJpaEntity> findByNameAndDeletedAtIsNull(String name);

    boolean existsByName(String name);


}

