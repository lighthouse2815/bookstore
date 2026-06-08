package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.command.DeleteNotificationCommand;
import com.bookstore.bookstore.application.command.MarkNotificationReadCommand;
import com.bookstore.bookstore.application.result.NotificationResult;
import java.util.List;
import java.util.UUID;

public interface INotificationService {

    List<NotificationResult> getMyNotifications(UUID userId);

    NotificationResult getMyNotification(UUID userId, UUID notificationId);

    NotificationResult markRead(MarkNotificationReadCommand command);

    void delete(DeleteNotificationCommand command);

    List<NotificationResult> getAll();

    NotificationResult getById(UUID notificationId);

    NotificationResult create(CreateNotificationCommand command);
}
