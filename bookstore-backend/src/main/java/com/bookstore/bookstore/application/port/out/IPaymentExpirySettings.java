package com.bookstore.bookstore.application.port.out;

public interface IPaymentExpirySettings {

    int bankTransferExpirationMinutes();

    boolean expiryJobEnabled();

    long expiryJobDelayMs();

    int expiryJobBatchSize();
}
