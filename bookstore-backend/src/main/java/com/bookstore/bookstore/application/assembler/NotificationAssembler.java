package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.NotificationResult;
import com.bookstore.bookstore.domain.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationAssembler {

    public NotificationResult toResult(Notification notification) {
        return new NotificationResult(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getReadAt(),
                notification.getType(),
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getLink()
        );
    }
}
