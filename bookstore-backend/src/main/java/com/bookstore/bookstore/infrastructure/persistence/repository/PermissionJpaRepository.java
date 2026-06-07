package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.infrastructure.persistence.entity.PermissionJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, UUID> {

    @Query("""
            select p
            from PermissionJpaEntity p
            where p.deletedAt is null
            """)
    List<PermissionJpaEntity> findAllActive();

    @Query("""
            select p
            from PermissionJpaEntity p
            """)
    List<PermissionJpaEntity> findAllIncludingDeleted();

    @Query("""
            select p
            from PermissionJpaEntity p
            where p.deletedAt is null
              and p.id = :id
            """)
    Optional<PermissionJpaEntity> findByIdActive(@Param("id") UUID id);

    @Query("""
            select p
            from PermissionJpaEntity p
            where p.code = :code
            """)
    Optional<PermissionJpaEntity> findByCodeIncludingDeleted(@Param("code") PermissionCode code);

    @Query("""
            select case when count(p) > 0 then true else false end
            from PermissionJpaEntity p
            where p.id = :id
            """)
    boolean existsByIdIncludingDeleted(@Param("id") UUID id);

    @Query("""
            select p
            from PermissionJpaEntity p
            where p.deletedAt is null
              and p.code = :code
            """)
    Optional<PermissionJpaEntity> findByCodeActive(@Param("code") PermissionCode code);

    @Query("""
            select case when count(p) > 0 then true else false end
            from PermissionJpaEntity p
            where p.code = :code
            """)
    boolean existsByCodeIncludingDeleted(@Param("code") PermissionCode code);
}
