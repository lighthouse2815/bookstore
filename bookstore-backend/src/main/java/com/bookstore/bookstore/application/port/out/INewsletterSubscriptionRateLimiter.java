package com.bookstore.bookstore.application.port.out;

public interface INewsletterSubscriptionRateLimiter {

    void checkAllowed(String clientAddress, String normalizedEmail);
}
