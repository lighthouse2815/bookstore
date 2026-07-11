package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import java.util.Collection;
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
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    List<UserJpaEntity> findAllByDeletedAtIsNull();

    @Query(
            value = """
                    select u.id
                    from UserJpaEntity u
                    join u.roles r
                    where u.deletedAt is null
                      and r.deletedAt is null
                      and r.name = :roleName
                    order by u.createdAt desc, u.id desc
                    """,
            countQuery = """
                    select count(distinct u)
                    from UserJpaEntity u
                    join u.roles r
                    where u.deletedAt is null
                      and r.deletedAt is null
                      and r.name = :roleName
                    """
    )
    Page<UUID> findPageIdsByRoleNameActive(@Param("roleName") String roleName, Pageable pageable);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select distinct u
            from UserJpaEntity u
            where u.deletedAt is null
              and u.id in :userIds
            """)
    List<UserJpaEntity> findAllByIdInAndDeletedAtIsNull(@Param("userIds") Collection<UUID> userIds);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    List<UserJpaEntity> findAll();

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findByIdAndDeletedAtIsNull(UUID userId);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<UserJpaEntity> findById(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("select u from UserJpaEntity u where u.id = :userId")
    Optional<UserJpaEntity> findByIdForUpdate(@Param("userId") UUID userId);

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

    long countByDeletedAtIsNull();

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
