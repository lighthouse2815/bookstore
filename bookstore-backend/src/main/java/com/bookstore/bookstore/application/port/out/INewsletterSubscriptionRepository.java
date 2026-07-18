package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.NewsletterSubscription;
import java.util.Optional;

public interface INewsletterSubscriptionRepository {

    Optional<NewsletterSubscription> findByEmail(String email);

    Optional<NewsletterSubscription> findByUnsubscribeToken(String unsubscribeToken);

    NewsletterSubscription save(NewsletterSubscription subscription);
}
