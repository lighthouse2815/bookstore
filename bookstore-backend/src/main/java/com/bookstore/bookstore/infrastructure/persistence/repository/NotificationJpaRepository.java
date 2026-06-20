package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.NotificationJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    List<NotificationJpaEntity> findAllByDeletedAtIsNullAndReadOrderByCreatedAtDesc(
            @Param("read") boolean read
    );

    Page<NotificationJpaEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    long countByDeletedAtIsNull();

    List<NotificationJpaEntity> findAllByDeletedAtIsNullAndUserIdOrderByCreatedAtDesc(
            @Param("userId") UUID userId
    );

    List<NotificationJpaEntity> findAllByDeletedAtIsNullAndUserIdAndReadOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            @Param("read") boolean read
    );

    Page<NotificationJpaEntity> findAllByDeletedAtIsNullAndUserIdOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    Page<NotificationJpaEntity> findAllByDeletedAtIsNullAndUserIdAndReadOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            @Param("read") boolean read,
            Pageable pageable
    );

    long countByDeletedAtIsNullAndUserId(@Param("userId") UUID userId);

    long countByDeletedAtIsNullAndUserIdAndRead(
            @Param("userId") UUID userId,
            @Param("read") boolean read
    );

    long countByDeletedAtIsNullAndUserIdAndReadFalse(@Param("userId") UUID userId);

    Optional<NotificationJpaEntity> findByDeletedAtIsNullAndId(@Param("notificationId") UUID notificationId);

    Optional<NotificationJpaEntity> findByDeletedAtIsNullAndIdAndUserId(
            @Param("notificationId") UUID notificationId,
            @Param("userId") UUID userId
    );

    Optional<NotificationJpaEntity> findById(@Param("notificationId") UUID notificationId);

}






