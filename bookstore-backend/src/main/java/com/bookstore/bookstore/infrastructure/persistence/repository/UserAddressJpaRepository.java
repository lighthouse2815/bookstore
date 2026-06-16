package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.UserAddressJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAddressJpaRepository extends JpaRepository<UserAddressJpaEntity, UUID> {

    @EntityGraph(attributePaths = "user")
    @Query("""
            select ua
            from UserAddressJpaEntity ua
            where ua.deletedAt is null
              and ua.user.deletedAt is null
              and ua.user.id = :userId
            order by ua.defaultAddress desc, ua.createdAt asc
            """)
    List<UserAddressJpaEntity> findAllByUser_IdActive(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = "user")
    Optional<UserAddressJpaEntity> findByIdAndDeletedAtIsNullAndUser_IdAndUser_DeletedAtIsNull(UUID id, UUID userId);

    @EntityGraph(attributePaths = "user")
    Optional<UserAddressJpaEntity> findById(UUID id);
}
