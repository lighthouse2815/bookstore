package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface INotificationRepository {

    List<Notification> findAllActive();

    List<Notification> findAllActive(Boolean read);

    List<Notification> findPageActive(int page, int size);

    long countActive();

    List<Notification> findAllByUserIdActive(UUID userId);

    List<Notification> findAllByUserIdActive(UUID userId, Boolean read);

    List<Notification> findPageByUserIdActive(UUID userId, Boolean read, int page, int size);

    long countByUserIdActive(UUID userId, Boolean read);

    long countUnreadByUserIdActive(UUID userId);

    List<Notification> findAllUnreadByUserIdActive(UUID userId);

    Optional<Notification> findByIdActive(UUID notificationId);

    Optional<Notification> findByIdAndUserIdActive(UUID notificationId, UUID userId);

    Optional<Notification> findByIdIncludingDeleted(UUID notificationId);

    Notification save(Notification notification);

    List<Notification> saveAll(List<Notification> notifications);
}
