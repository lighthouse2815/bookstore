package com.bookstore.bookstore.infrastructure.outbox;

import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.command.OutboxNotificationPayload;
import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.application.port.out.IOutboxDeliveryRepository;
import com.bookstore.bookstore.domain.model.OutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxEventDeliveryService {
    private static final String IN_APP_NOTIFICATION_CONSUMER = "IN_APP_NOTIFICATION";
    private final IOutboxDeliveryRepository outboxDeliveryRepository;
    private final INotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void deliver(OutboxEvent event) {
        if (outboxDeliveryRepository.exists(event.getId(), IN_APP_NOTIFICATION_CONSUMER)) return;
        OutboxNotificationPayload payload = parseNotification(event);
        notificationService.create(new CreateNotificationCommand(payload.userId(), payload.title(), payload.content(),
                payload.type(), payload.targetType(), payload.targetId(), payload.link()));
        outboxDeliveryRepository.save(event.getId(), IN_APP_NOTIFICATION_CONSUMER, Instant.now());
    }

    private OutboxNotificationPayload parseNotification(OutboxEvent event) {
        try {
            OutboxNotificationPayload payload = objectMapper.readValue(event.getPayload(), OutboxNotificationPayload.class);
            if (payload == null || payload.userId() == null) throw new IllegalArgumentException("notification recipient is required");
            return payload;
        } catch (Exception exception) {
            throw new IllegalArgumentException("unsupported outbox payload for event " + event.getEventType(), exception);
        }
    }
}
