package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.SubscribeNewsletterCommand;
import com.bookstore.bookstore.application.command.UnsubscribeNewsletterCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.INewsletterSubscriptionRateLimiter;
import com.bookstore.bookstore.application.port.out.INewsletterSubscriptionRepository;
import com.bookstore.bookstore.domain.enums.NewsletterSubscriptionStatus;
import com.bookstore.bookstore.domain.model.NewsletterSubscription;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewsletterSubscriptionServiceTest {

    @Mock
    private INewsletterSubscriptionRepository newsletterSubscriptionRepository;

    @Mock
    private INewsletterSubscriptionRateLimiter newsletterSubscriptionRateLimiter;

    private NewsletterSubscriptionService newsletterSubscriptionService;

    @BeforeEach
    void setUp() {
        newsletterSubscriptionService = new NewsletterSubscriptionService(
                newsletterSubscriptionRepository,
                newsletterSubscriptionRateLimiter
        );
    }

    @Test
    void subscribe_newEmail_createsActiveNormalizedSubscription() {
        when(newsletterSubscriptionRepository.findByEmail("reader@example.com"))
                .thenReturn(Optional.empty());
        when(newsletterSubscriptionRepository.save(any(NewsletterSubscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NewsletterSubscription result = newsletterSubscriptionService.subscribe(
                new SubscribeNewsletterCommand("  Reader@Example.com ", "127.0.0.1")
        );

        assertEquals("reader@example.com", result.getEmail());
        assertEquals(NewsletterSubscriptionStatus.ACTIVE, result.getStatus());
        assertNull(result.getUnsubscribedAt());
        verify(newsletterSubscriptionRateLimiter)
                .checkAllowed("127.0.0.1", "reader@example.com");
        verify(newsletterSubscriptionRepository).save(result);
    }

    @Test
    void subscribe_existingActiveEmail_isIdempotent() {
        NewsletterSubscription existing = activeSubscription("reader@example.com");
        when(newsletterSubscriptionRepository.findByEmail("reader@example.com"))
                .thenReturn(Optional.of(existing));

        NewsletterSubscription result = newsletterSubscriptionService.subscribe(
                new SubscribeNewsletterCommand("reader@example.com", "127.0.0.1")
        );

        assertSame(existing, result);
        verify(newsletterSubscriptionRepository, never()).save(any());
    }

    @Test
    void subscribe_unsubscribedEmail_reactivatesWithoutCreatingDuplicate() {
        NewsletterSubscription existing = unsubscribedSubscription("reader@example.com");
        String previousToken = existing.getUnsubscribeToken();
        when(newsletterSubscriptionRepository.findByEmail("reader@example.com"))
                .thenReturn(Optional.of(existing));
        when(newsletterSubscriptionRepository.save(existing)).thenReturn(existing);

        NewsletterSubscription result = newsletterSubscriptionService.subscribe(
                new SubscribeNewsletterCommand("reader@example.com", "127.0.0.1")
        );

        assertEquals(NewsletterSubscriptionStatus.ACTIVE, result.getStatus());
        assertNull(result.getUnsubscribedAt());
        assertNotEquals(previousToken, result.getUnsubscribeToken());
        verify(newsletterSubscriptionRepository).save(existing);
    }

    @Test
    void unsubscribe_activeSubscription_marksItUnsubscribed() {
        NewsletterSubscription existing = activeSubscription("reader@example.com");
        when(newsletterSubscriptionRepository.findByUnsubscribeToken(existing.getUnsubscribeToken()))
                .thenReturn(Optional.of(existing));
        when(newsletterSubscriptionRepository.save(existing)).thenReturn(existing);

        NewsletterSubscription result = newsletterSubscriptionService.unsubscribe(
                new UnsubscribeNewsletterCommand(existing.getUnsubscribeToken())
        );

        assertEquals(NewsletterSubscriptionStatus.UNSUBSCRIBED, result.getStatus());
        verify(newsletterSubscriptionRepository).save(existing);
    }

    @Test
    void unsubscribe_unknownToken_returnsNotFound() {
        when(newsletterSubscriptionRepository.findByUnsubscribeToken("missing-token"))
                .thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> newsletterSubscriptionService.unsubscribe(
                        new UnsubscribeNewsletterCommand("missing-token")
                )
        );

        assertEquals(ApplicationErrorCode.NEWSLETTER_SUBSCRIPTION_NOT_FOUND, exception.getErrorCode());
    }

    private NewsletterSubscription activeSubscription(String email) {
        Instant now = Instant.now().minus(1, ChronoUnit.SECONDS);
        return new NewsletterSubscription(
                UUID.randomUUID(),
                email,
                NewsletterSubscriptionStatus.ACTIVE,
                UUID.randomUUID().toString(),
                now,
                null,
                now,
                now,
                null
        );
    }

    private NewsletterSubscription unsubscribedSubscription(String email) {
        Instant createdAt = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant unsubscribedAt = Instant.now().minus(1, ChronoUnit.DAYS);
        return new NewsletterSubscription(
                UUID.randomUUID(),
                email,
                NewsletterSubscriptionStatus.UNSUBSCRIBED,
                UUID.randomUUID().toString(),
                createdAt,
                unsubscribedAt,
                createdAt,
                unsubscribedAt,
                null
        );
    }
}
