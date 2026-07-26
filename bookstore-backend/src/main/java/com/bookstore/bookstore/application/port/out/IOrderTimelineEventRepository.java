package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.OrderTimelineEvent;
import java.util.List;
import java.util.UUID;

public interface IOrderTimelineEventRepository {

    OrderTimelineEvent save(OrderTimelineEvent event);

    List<OrderTimelineEvent> findByOrderId(UUID orderId);
}
