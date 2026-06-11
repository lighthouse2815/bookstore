package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.VerifyOtpCommand;
import com.bookstore.bookstore.domain.model.User;

public interface IOtpService {

    void sendRegistrationOtp(User user);

    void verifyRegistrationOtp(VerifyOtpCommand command);
}
