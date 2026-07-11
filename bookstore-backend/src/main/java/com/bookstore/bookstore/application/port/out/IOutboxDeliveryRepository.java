package com.bookstore.bookstore.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface IOutboxDeliveryRepository {
    boolean exists(UUID eventId, String consumer);
    void save(UUID eventId, String consumer, Instant deliveredAt);
}
