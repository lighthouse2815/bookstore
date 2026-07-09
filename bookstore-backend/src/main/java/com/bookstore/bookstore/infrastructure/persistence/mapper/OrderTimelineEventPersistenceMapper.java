package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.OrderTimelineEvent;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderTimelineEventJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderTimelineEventPersistenceMapper {

    public OrderTimelineEvent toDomain(OrderTimelineEventJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new OrderTimelineEvent(
                entity.getId(),
                entity.getOrder().getId(),
                entity.getActorId(),
                entity.getActorName(),
                entity.getActorRole(),
                entity.getEventType(),
                entity.getOldStatus(),
                entity.getNewStatus(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getMetadata(),
                entity.getCreatedAt()
        );
    }

    public void copyToEntity(
            OrderTimelineEvent event,
            OrderTimelineEventJpaEntity entity,
            OrderJpaEntity order
    ) {
        entity.setId(event.getId());
        entity.setOrder(order);
        entity.setActorId(event.getActorId());
        entity.setActorName(event.getActorName());
        entity.setActorRole(event.getActorRole());
        entity.setEventType(event.getEventType());
        entity.setOldStatus(event.getOldStatus());
        entity.setNewStatus(event.getNewStatus());
        entity.setTitle(event.getTitle());
        entity.setDescription(event.getDescription());
        entity.setMetadata(event.getMetadata());
        entity.setCreatedAt(event.getCreatedAt());
    }
}
