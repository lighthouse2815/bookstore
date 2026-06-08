package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.SupplierJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, UUID> {

    @Query("""
            select s
            from SupplierJpaEntity s
            where s.deletedAt is null
            order by s.createdAt desc
            """)
    List<SupplierJpaEntity> findAllActive();

    @Query("""
            select s
            from SupplierJpaEntity s
            where s.deletedAt is null
              and s.id = :id
            """)
    Optional<SupplierJpaEntity> findByIdActive(@Param("id") UUID id);

    @Query("""
            select s
            from SupplierJpaEntity s
            where s.id = :id
            """)
    Optional<SupplierJpaEntity> findByIdIncludingDeleted(@Param("id") UUID id);

    @Query("""
            select case when count(s) > 0 then true else false end
            from SupplierJpaEntity s
            where s.name = :name
            """)
    boolean existsByNameIncludingDeleted(@Param("name") String name);
}
