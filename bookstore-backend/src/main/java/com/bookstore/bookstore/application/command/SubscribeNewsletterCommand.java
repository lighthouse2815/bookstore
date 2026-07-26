package com.bookstore.bookstore.application.command;

public record SubscribeNewsletterCommand(
        String email,
        String clientAddress
) {
}
