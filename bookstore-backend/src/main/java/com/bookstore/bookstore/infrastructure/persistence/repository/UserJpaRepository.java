package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    List<UserJpaEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    List<UserJpaEntity> findAll();

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findByIdAndDeletedAtIsNull(UUID userId);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findById(UUID userId);

    boolean existsById(UUID userId);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findByUsernameAndDeletedAtIsNull(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findByUsername(String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);
}
