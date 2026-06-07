package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.infrastructure.persistence.entity.RoleJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, UUID> {

    @EntityGraph(attributePaths = "permissions")
    @Query("""
            select r
            from RoleJpaEntity r
            where r.deletedAt is null
            """)
    List<RoleJpaEntity> findAllActive();

    @EntityGraph(attributePaths = "permissions")
    @Query("""
            select r
            from RoleJpaEntity r
            """)
    List<RoleJpaEntity> findAllIncludingDeleted();

    @EntityGraph(attributePaths = "permissions")
    @Query("""
            select r
            from RoleJpaEntity r
            where r.deletedAt is null
              and r.id = :roleId
            """)
    Optional<RoleJpaEntity> findByIdActive(@Param("roleId") UUID roleId);

    @EntityGraph(attributePaths = "permissions")
    @Query("""
            select r
            from RoleJpaEntity r
            where r.id = :roleId
            """)
    Optional<RoleJpaEntity> findByIdIncludingDeleted(@Param("roleId") UUID roleId);

    @Query("""
            select case when count(r) > 0 then true else false end
            from RoleJpaEntity r
            where r.id = :roleId
            """)
    boolean existsByIdIncludingDeleted(@Param("roleId") UUID roleId);

    @EntityGraph(attributePaths = "permissions")
    @Query("""
            select r
            from RoleJpaEntity r
            where r.deletedAt is null
              and r.name = :roleName
            """)
    Optional<RoleJpaEntity> findByNameActive(@Param("roleName") String roleName);

    @Query("""
            select case when count(r) > 0 then true else false end
            from RoleJpaEntity r
            where r.name = :roleName
            """)
    boolean existsByNameIncludingDeleted(@Param("roleName") String roleName);

    @Query("""
            select case when count(r) > 0 then true else false end
            from RoleJpaEntity r
            join r.permissions p
            where p.code = :permissionCode
            """)
    boolean existsByPermissionsCodeIncludingDeleted(@Param("permissionCode") PermissionCode permissionCode);
}
