package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    void markRead_setsReadState() {
        Notification notification = notification();

        notification.markRead();

        assertEquals(true, notification.isRead());
        assertNotNull(notification.getReadAt());
    }

    @Test
    void markRead_whenAlreadyRead_rejects() {
        Instant now = Instant.now();
        Notification notification = new Notification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Title",
                "Content",
                true,
                now.minusSeconds(10),
                now,
                now,
                null
        );

        DomainException exception = assertThrows(DomainException.class, notification::markRead);

        assertEquals(DomainErrorCode.NOTIFICATION_ALREADY_READ, exception.getErrorCode());
    }

    private static Notification notification() {
        Instant now = Instant.EPOCH;
        return new Notification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Title",
                "Content",
                false,
                now,
                now,
                null,
                null
        );
    }
}
