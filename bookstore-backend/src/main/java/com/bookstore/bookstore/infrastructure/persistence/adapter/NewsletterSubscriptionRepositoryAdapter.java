package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.INewsletterSubscriptionRepository;
import com.bookstore.bookstore.domain.model.NewsletterSubscription;
import com.bookstore.bookstore.infrastructure.persistence.entity.NewsletterSubscriptionJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.NewsletterSubscriptionPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.NewsletterSubscriptionJpaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NewsletterSubscriptionRepositoryAdapter implements INewsletterSubscriptionRepository {

    private final NewsletterSubscriptionJpaRepository newsletterSubscriptionJpaRepository;
    private final NewsletterSubscriptionPersistenceMapper newsletterSubscriptionPersistenceMapper;

    @Override
    public Optional<NewsletterSubscription> findByEmail(String email) {
        return newsletterSubscriptionJpaRepository.findByEmailAndDeletedAtIsNull(email)
                .map(newsletterSubscriptionPersistenceMapper::toDomain);
    }

    @Override
    public Optional<NewsletterSubscription> findByUnsubscribeToken(String unsubscribeToken) {
        return newsletterSubscriptionJpaRepository.findByUnsubscribeTokenAndDeletedAtIsNull(unsubscribeToken)
                .map(newsletterSubscriptionPersistenceMapper::toDomain);
    }

    @Override
    public NewsletterSubscription save(NewsletterSubscription subscription) {
        NewsletterSubscriptionJpaEntity entity = newsletterSubscriptionJpaRepository
                .findById(subscription.getId())
                .orElseGet(NewsletterSubscriptionJpaEntity::new);
        newsletterSubscriptionPersistenceMapper.copyToEntity(entity, subscription);
        return newsletterSubscriptionPersistenceMapper.toDomain(
                newsletterSubscriptionJpaRepository.save(entity)
        );
    }
}
