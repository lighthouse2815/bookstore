package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.NewsletterSubscription;
import com.bookstore.bookstore.infrastructure.persistence.entity.NewsletterSubscriptionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class NewsletterSubscriptionPersistenceMapper {

    public NewsletterSubscription toDomain(NewsletterSubscriptionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new NewsletterSubscription(
                entity.getId(),
                entity.getEmail(),
                entity.getStatus(),
                entity.getUnsubscribeToken(),
                entity.getSubscribedAt(),
                entity.getUnsubscribedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            NewsletterSubscriptionJpaEntity entity,
            NewsletterSubscription subscription
    ) {
        entity.setId(subscription.getId());
        entity.setEmail(subscription.getEmail());
        entity.setStatus(subscription.getStatus());
        entity.setUnsubscribeToken(subscription.getUnsubscribeToken());
        entity.setSubscribedAt(subscription.getSubscribedAt());
        entity.setUnsubscribedAt(subscription.getUnsubscribedAt());
        entity.setCreatedAt(subscription.getCreatedAt());
        entity.setUpdatedAt(subscription.getUpdatedAt());
        entity.setDeletedAt(subscription.getDeletedAt());
    }
}
