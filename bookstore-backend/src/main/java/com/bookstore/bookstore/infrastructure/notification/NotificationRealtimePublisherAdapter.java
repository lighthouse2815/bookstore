package com.bookstore.bookstore.infrastructure.notification;

import com.bookstore.bookstore.application.port.out.INotificationRealtimePublisher;
import com.bookstore.bookstore.application.result.NotificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationRealtimePublisherAdapter implements INotificationRealtimePublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationRealtimePublisherAdapter.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationRealtimePublisherAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishToUser(String userId, NotificationResult notification) {
        try {
            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
        } catch (RuntimeException exception) {
            log.warn("Failed to publish realtime notification {} to user {}", notification.notificationId(), userId, exception);
        }
    }
}
