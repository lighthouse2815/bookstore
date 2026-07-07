package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    List<UserJpaEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select distinct u
            from UserJpaEntity u
            join u.roles r
            where u.deletedAt is null
              and r.deletedAt is null
              and r.name = :roleName
            order by u.createdAt desc
            """)
    Page<UserJpaEntity> findPageByRoleNameActive(@Param("roleName") String roleName, Pageable pageable);

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

    @Query("""
            select count(distinct u)
            from UserJpaEntity u
            join u.roles r
            where u.deletedAt is null
              and r.deletedAt is null
              and r.name = 'USER'
              and u.createdAt >= :fromInclusive
              and u.createdAt < :toExclusive
            """)
    long countNewCustomersBetween(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive
    );
}
