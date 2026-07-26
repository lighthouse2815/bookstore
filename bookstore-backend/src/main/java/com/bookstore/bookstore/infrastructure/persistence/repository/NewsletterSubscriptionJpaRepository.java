package com.bookstore.bookstore.infrastructure.persistence.repository;

import com.bookstore.bookstore.infrastructure.persistence.entity.NewsletterSubscriptionJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionJpaRepository
        extends JpaRepository<NewsletterSubscriptionJpaEntity, UUID> {

    Optional<NewsletterSubscriptionJpaEntity> findByEmailAndDeletedAtIsNull(String email);

    Optional<NewsletterSubscriptionJpaEntity> findByUnsubscribeTokenAndDeletedAtIsNull(String unsubscribeToken);
}
