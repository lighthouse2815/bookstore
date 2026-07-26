package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.SubscribeNewsletterCommand;
import com.bookstore.bookstore.application.command.UnsubscribeNewsletterCommand;
import com.bookstore.bookstore.domain.model.NewsletterSubscription;

public interface INewsletterSubscriptionService {

    NewsletterSubscription subscribe(SubscribeNewsletterCommand command);

    NewsletterSubscription unsubscribe(UnsubscribeNewsletterCommand command);
}
