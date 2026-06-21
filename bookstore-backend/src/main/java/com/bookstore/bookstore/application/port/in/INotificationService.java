package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.BroadcastNotificationCommand;
import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.command.DeleteNotificationCommand;
import com.bookstore.bookstore.application.command.MarkNotificationReadCommand;
import com.bookstore.bookstore.application.result.NotificationBroadcastResult;
import com.bookstore.bookstore.application.result.NotificationResult;
import com.bookstore.bookstore.application.result.NotificationSliceResult;
import java.util.List;
import java.util.UUID;

public interface INotificationService {

    List<NotificationResult> getMyNotifications(UUID userId);

    List<NotificationResult> getMyNotifications(UUID userId, Boolean read);

    NotificationSliceResult getMyNotifications(UUID userId, int page, int size, Boolean read);

    long countMyUnreadNotifications(UUID userId);

    NotificationResult getMyNotification(UUID userId, UUID notificationId);

    NotificationResult markRead(MarkNotificationReadCommand command);

    void markAllRead(UUID userId);

    void delete(DeleteNotificationCommand command);

    List<NotificationResult> getAll();

    NotificationSliceResult getAll(int page, int size);

    NotificationResult getById(UUID notificationId);

    NotificationResult create(CreateNotificationCommand command);

    NotificationBroadcastResult broadcast(BroadcastNotificationCommand command);

    void adminDelete(UUID notificationId);
}
