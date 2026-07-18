package com.bookstore.bookstore.application.port.out;

public interface IAiChatSettings {

    boolean enabled();

    int historyLimit();

    int dailyUserLimit();
}
