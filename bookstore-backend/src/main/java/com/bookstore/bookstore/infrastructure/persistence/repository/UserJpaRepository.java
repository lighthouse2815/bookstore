package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select u
            from UserJpaEntity u
            where u.deletedAt is null
            """)
    List<UserJpaEntity> findAllActive();

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select u
            from UserJpaEntity u
            """)
    List<UserJpaEntity> findAllIncludingDeleted();

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select u
            from UserJpaEntity u
            where u.deletedAt is null
              and u.id = :userId
            """)
    Optional<UserJpaEntity> findByIdActive(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select u
            from UserJpaEntity u
            where u.id = :userId
            """)
    Optional<UserJpaEntity> findByIdIncludingDeleted(@Param("userId") UUID userId);

    @Query("""
            select case when count(u) > 0 then true else false end
            from UserJpaEntity u
            where u.id = :userId
            """)
    boolean existsByIdIncludingDeleted(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select u
            from UserJpaEntity u
            where u.deletedAt is null
              and u.username = :username
            """)
    Optional<UserJpaEntity> findByUsernameActive(@Param("username") String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select u
            from UserJpaEntity u
            where u.username = :username
            """)
    Optional<UserJpaEntity> findByUsernameIncludingDeleted(@Param("username") String username);

    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    @Query("""
            select u
            from UserJpaEntity u
            where u.email = :email
            """)
    Optional<UserJpaEntity> findByEmailIncludingDeleted(@Param("email") String email);

    @Query("""
            select case when count(u) > 0 then true else false end
            from UserJpaEntity u
            where u.username = :username
            """)
    boolean existsByUsernameIncludingDeleted(@Param("username") String username);

    @Query("""
            select case when count(u) > 0 then true else false end
            from UserJpaEntity u
            where u.phoneNumber = :phoneNumber
            """)
    boolean existsByPhoneNumberIncludingDeleted(@Param("phoneNumber") String phoneNumber);

    @Query("""
            select case when count(u) > 0 then true else false end
            from UserJpaEntity u
            where u.email = :email
            """)
    boolean existsByEmailIncludingDeleted(@Param("email") String email);
}
