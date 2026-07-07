package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.AuthorJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorJpaRepository extends JpaRepository<AuthorJpaEntity, UUID> {

    @EntityGraph(attributePaths = "avatarFileAsset")
    List<AuthorJpaEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = "avatarFileAsset")
    Page<AuthorJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "avatarFileAsset")
    List<AuthorJpaEntity> findAll();

    @EntityGraph(attributePaths = "avatarFileAsset")
    Optional<AuthorJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    @EntityGraph(attributePaths = "avatarFileAsset")
    Optional<AuthorJpaEntity> findById(UUID id);

    boolean existsById(UUID id);

    @EntityGraph(attributePaths = "avatarFileAsset")
    Optional<AuthorJpaEntity> findByNameAndDeletedAtIsNull(String name);

    boolean existsByName(String name);


}
