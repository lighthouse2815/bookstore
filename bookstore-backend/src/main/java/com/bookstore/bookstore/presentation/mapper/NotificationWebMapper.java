package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.command.DeleteNotificationCommand;
import com.bookstore.bookstore.application.command.MarkNotificationReadCommand;
import com.bookstore.bookstore.application.result.NotificationResult;
import com.bookstore.bookstore.presentation.request.CreateNotificationRequest;
import com.bookstore.bookstore.presentation.response.NotificationResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class NotificationWebMapper {

    public CreateNotificationCommand toCreateCommand(CreateNotificationRequest request) {
        return new CreateNotificationCommand(
                request.userId(),
                request.title(),
                request.content()
        );
    }

    public MarkNotificationReadCommand toMarkReadCommand(UUID notificationId, UUID userId) {
        return new MarkNotificationReadCommand(notificationId, userId);
    }

    public DeleteNotificationCommand toDeleteCommand(UUID notificationId, UUID userId) {
        return new DeleteNotificationCommand(notificationId, userId);
    }

    public NotificationResponse toResponse(NotificationResult result) {
        return new NotificationResponse(
                result.notificationId(),
                result.userId(),
                result.title(),
                result.content(),
                result.read(),
                result.createdAt(),
                result.updatedAt(),
                result.readAt()
        );
    }
}
