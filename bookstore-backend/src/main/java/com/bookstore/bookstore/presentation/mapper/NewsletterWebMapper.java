package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.SubscribeNewsletterCommand;
import com.bookstore.bookstore.application.command.UnsubscribeNewsletterCommand;
import com.bookstore.bookstore.presentation.request.SubscribeNewsletterRequest;
import com.bookstore.bookstore.presentation.request.UnsubscribeNewsletterRequest;
import org.springframework.stereotype.Component;

@Component
public class NewsletterWebMapper {

    public SubscribeNewsletterCommand toSubscribeCommand(
            SubscribeNewsletterRequest request,
            String clientAddress
    ) {
        return new SubscribeNewsletterCommand(request.email(), clientAddress);
    }

    public UnsubscribeNewsletterCommand toUnsubscribeCommand(UnsubscribeNewsletterRequest request) {
        return new UnsubscribeNewsletterCommand(request.token());
    }
}
