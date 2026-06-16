package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.domain.enums.PermissionCode;
import com.bookstore.bookstore.infrastructure.persistence.entity.RoleJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, UUID> {

    @EntityGraph(attributePaths = "permissions")
    List<RoleJpaEntity> findAllByDeletedAtIsNull();

    @EntityGraph(attributePaths = "permissions")
    List<RoleJpaEntity> findAll();

    @EntityGraph(attributePaths = "permissions")
    Optional<RoleJpaEntity> findByIdAndDeletedAtIsNull(UUID roleId);

    @EntityGraph(attributePaths = "permissions")
    Optional<RoleJpaEntity> findById(@Param("roleId") UUID roleId);

    boolean existsById(@Param("roleId") UUID roleId);

    @EntityGraph(attributePaths = "permissions")
    Optional<RoleJpaEntity> findByNameAndDeletedAtIsNull(String roleName);

    boolean existsByName(@Param("roleName") String roleName);

    boolean existsByPermissions_Code(PermissionCode permissionCode);
}
