package com.bookstore.bookstore.application.port.out;

public interface ISepaySettings {

    String merchantId();

    String secretKey();

    String webhookApiKey();
}
