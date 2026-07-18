package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.SubscribeNewsletterCommand;
import com.bookstore.bookstore.application.command.UnsubscribeNewsletterCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.INewsletterSubscriptionService;
import com.bookstore.bookstore.application.port.out.INewsletterSubscriptionRateLimiter;
import com.bookstore.bookstore.application.port.out.INewsletterSubscriptionRepository;
import com.bookstore.bookstore.domain.enums.NewsletterSubscriptionStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.model.NewsletterSubscription;
import com.bookstore.bookstore.domain.validation.Guard;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsletterSubscriptionService implements INewsletterSubscriptionService {

    private final INewsletterSubscriptionRepository newsletterSubscriptionRepository;
    private final INewsletterSubscriptionRateLimiter newsletterSubscriptionRateLimiter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NewsletterSubscription subscribe(SubscribeNewsletterCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String normalizedEmail = normalizeEmail(command.email());
        newsletterSubscriptionRateLimiter.checkAllowed(command.clientAddress(), normalizedEmail);

        return newsletterSubscriptionRepository.findByEmail(normalizedEmail)
                .map(this::reactivateIfNeeded)
                .orElseGet(() -> createActiveSubscription(normalizedEmail));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NewsletterSubscription unsubscribe(UnsubscribeNewsletterCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String token = StringUtils.trimToNull(command.token());
        if (token == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "token");
        }

        NewsletterSubscription subscription = newsletterSubscriptionRepository.findByUnsubscribeToken(token)
                .orElseThrow(() -> new ApplicationException(
                        ApplicationErrorCode.NEWSLETTER_SUBSCRIPTION_NOT_FOUND
                ));

        if (subscription.getStatus() == NewsletterSubscriptionStatus.UNSUBSCRIBED) {
            return subscription;
        }

        subscription.unsubscribe();
        return newsletterSubscriptionRepository.save(subscription);
    }

    private NewsletterSubscription reactivateIfNeeded(NewsletterSubscription subscription) {
        if (subscription.getStatus() == NewsletterSubscriptionStatus.ACTIVE) {
            return subscription;
        }

        subscription.reactivate(newUnsubscribeToken());
        return newsletterSubscriptionRepository.save(subscription);
    }

    private NewsletterSubscription createActiveSubscription(String normalizedEmail) {
        Instant now = Instant.now();
        NewsletterSubscription subscription = new NewsletterSubscription(
                UUID.randomUUID(),
                normalizedEmail,
                NewsletterSubscriptionStatus.ACTIVE,
                newUnsubscribeToken(),
                now,
                null,
                now,
                now,
                null
        );
        return newsletterSubscriptionRepository.save(subscription);
    }

    private String normalizeEmail(String email) {
        String validEmail = Guard.email(
                StringUtils.trimToNull(email),
                DomainErrorCode.INVALID_NEWSLETTER_SUBSCRIPTION_EMAIL,
                "email"
        );
        return validEmail.toLowerCase(Locale.ROOT);
    }

    private String newUnsubscribeToken() {
        return UUID.randomUUID().toString();
    }
}
