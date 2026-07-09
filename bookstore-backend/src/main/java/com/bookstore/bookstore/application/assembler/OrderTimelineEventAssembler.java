package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.OrderTimelineEventResult;
import com.bookstore.bookstore.domain.model.OrderTimelineEvent;
import org.springframework.stereotype.Component;

@Component
public class OrderTimelineEventAssembler {

    public OrderTimelineEventResult toResult(OrderTimelineEvent event) {
        if (event == null) {
            return null;
        }

        return new OrderTimelineEventResult(
                event.getId(),
                event.getOrderId(),
                event.getEventType(),
                event.getTitle(),
                event.getDescription(),
                event.getOldStatus(),
                event.getNewStatus(),
                event.getActorName(),
                event.getActorRole(),
                event.getCreatedAt(),
                event.getMetadata()
        );
    }
}
