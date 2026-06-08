package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface INotificationRepository {

    List<Notification> findAllActive();

    List<Notification> findAllByUserIdActive(UUID userId);

    Optional<Notification> findByIdActive(UUID notificationId);

    Optional<Notification> findByIdAndUserIdActive(UUID notificationId, UUID userId);

    Optional<Notification> findByIdIncludingDeleted(UUID notificationId);

    Notification save(Notification notification);
}
