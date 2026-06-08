package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.NotificationJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    @Query("""
            select n
            from NotificationJpaEntity n
            where n.deletedAt is null
            order by n.createdAt desc
            """)
    List<NotificationJpaEntity> findAllActive();

    @Query("""
            select n
            from NotificationJpaEntity n
            where n.deletedAt is null
              and n.userId = :userId
            order by n.createdAt desc
            """)
    List<NotificationJpaEntity> findAllByUserIdActive(@Param("userId") UUID userId);

    @Query("""
            select n
            from NotificationJpaEntity n
            where n.deletedAt is null
              and n.id = :notificationId
            """)
    Optional<NotificationJpaEntity> findByIdActive(@Param("notificationId") UUID notificationId);

    @Query("""
            select n
            from NotificationJpaEntity n
            where n.deletedAt is null
              and n.id = :notificationId
              and n.userId = :userId
            """)
    Optional<NotificationJpaEntity> findByIdAndUserIdActive(
            @Param("notificationId") UUID notificationId,
            @Param("userId") UUID userId
    );

    @Query("""
            select n
            from NotificationJpaEntity n
            where n.id = :notificationId
            """)
    Optional<NotificationJpaEntity> findByIdIncludingDeleted(@Param("notificationId") UUID notificationId);
}
