package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.CategoryJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    @Query("""
            select c
            from CategoryJpaEntity c
            where c.deletedAt is null
            """)
    List<CategoryJpaEntity> findAllActive();

    @Query("""
            select c
            from CategoryJpaEntity c
            """)
    List<CategoryJpaEntity> findAllIncludingDeleted();

    @Query("""
            select c
            from CategoryJpaEntity c
            where c.deletedAt is null
              and c.id = :id
            """)
    Optional<CategoryJpaEntity> findByIdActive(@Param("id") UUID id);

    @Query("""
            select c
            from CategoryJpaEntity c
            where c.id = :id
            """)
    Optional<CategoryJpaEntity> findByIdIncludingDeleted(@Param("id") UUID id);

    @Query("""
            select case when count(c) > 0 then true else false end
            from CategoryJpaEntity c
            where c.id = :id
            """)
    boolean existsByIdIncludingDeleted(@Param("id") UUID id);

    @Query("""
            select c
            from CategoryJpaEntity c
            where c.deletedAt is null
              and c.name = :name
            """)
    Optional<CategoryJpaEntity> findByNameActive(@Param("name") String name);

    @Query("""
            select case when count(c) > 0 then true else false end
            from CategoryJpaEntity c
            where c.name = :name
            """)
    boolean existsByNameIncludingDeleted(@Param("name") String name);
}
