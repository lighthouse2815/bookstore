package com.bookstore.bookstore.application.port.out;

public interface IEmailSender {

    void sendOtpEmail(String recipientEmail, String otpCode, long expirationMinutes);
}
