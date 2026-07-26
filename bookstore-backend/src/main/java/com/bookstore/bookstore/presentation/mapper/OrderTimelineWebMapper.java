package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.result.OrderTimelineEventResult;
import com.bookstore.bookstore.presentation.response.OrderTimelineEventResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderTimelineWebMapper {

    public OrderTimelineEventResponse toResponse(OrderTimelineEventResult result) {
        return new OrderTimelineEventResponse(
                result.id(),
                result.orderId(),
                result.eventType(),
                result.title(),
                result.description(),
                result.oldStatus(),
                result.newStatus(),
                result.actorName(),
                result.actorRole(),
                result.createdAt(),
                result.metadata()
        );
    }
}
