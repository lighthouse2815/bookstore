package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.ProfileJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileJpaRepository extends JpaRepository<ProfileJpaEntity, UUID> {

    @EntityGraph(attributePaths = "user")
    @Query("""
            select p
            from ProfileJpaEntity p
            where p.deletedAt is null
            """)
    List<ProfileJpaEntity> findAllActive();

    @EntityGraph(attributePaths = "user")
    @Query("""
            select p
            from ProfileJpaEntity p
            """)
    List<ProfileJpaEntity> findAllIncludingDeleted();

    @EntityGraph(attributePaths = "user")
    @Query("""
            select p
            from ProfileJpaEntity p
            where p.deletedAt is null
              and p.id = :profileId
            """)
    Optional<ProfileJpaEntity> findByIdActive(@Param("profileId") UUID profileId);

    @EntityGraph(attributePaths = "user")
    @Query("""
            select p
            from ProfileJpaEntity p
            where p.id = :profileId
            """)
    Optional<ProfileJpaEntity> findByIdIncludingDeleted(@Param("profileId") UUID profileId);

    @EntityGraph(attributePaths = "user")
    @Query("""
            select p
            from ProfileJpaEntity p
            where p.deletedAt is null
              and p.user.id = :userId
            """)
    Optional<ProfileJpaEntity> findByUserIdActive(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = "user")
    @Query("""
            select case when count(p) > 0 then true else false end
            from ProfileJpaEntity p
            where p.id = :profileId
            """)
    boolean existsByIdIncludingDeleted(@Param("profileId") UUID profileId);

    @EntityGraph(attributePaths = "user")
    @Query("""
            select p
            from ProfileJpaEntity p
            where p.user.id = :userId
            """)
    Optional<ProfileJpaEntity> findByUserIdIncludingDeleted(@Param("userId") UUID userId);

    @Query("""
            select case when count(p) > 0 then true else false end
            from ProfileJpaEntity p
            where p.user.id = :userId
            """)
    boolean existsByUserIdIncludingDeleted(@Param("userId") UUID userId);
}
