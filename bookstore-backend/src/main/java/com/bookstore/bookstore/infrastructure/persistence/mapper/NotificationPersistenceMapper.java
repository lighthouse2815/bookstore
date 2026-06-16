package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Notification;
import com.bookstore.bookstore.infrastructure.persistence.entity.NotificationJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationPersistenceMapper {

    public Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Notification(
                entity.getId(),
                entity.getUser().getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.isRead(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getReadAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(NotificationJpaEntity entity, Notification notification, UserJpaEntity user) {
        entity.setId(notification.getId());
        entity.setUser(user);
        entity.setTitle(notification.getTitle());
        entity.setContent(notification.getContent());
        entity.setRead(notification.isRead());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setUpdatedAt(notification.getUpdatedAt());
        entity.setReadAt(notification.getReadAt());
        entity.setDeletedAt(notification.getDeletedAt());
    }
}
