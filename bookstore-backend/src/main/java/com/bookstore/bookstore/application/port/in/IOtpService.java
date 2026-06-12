package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.RequestRegistrationOtpCommand;
import com.bookstore.bookstore.application.command.VerifyOtpCommand;
import com.bookstore.bookstore.domain.model.User;

public interface IOtpService {

    void requestRegistrationOtp(RequestRegistrationOtpCommand command);

    void sendRegistrationOtp(User user);

    void sendPasswordResetOtp(User user);

    void verifyRegistrationOtp(VerifyOtpCommand command);

    User verifyPasswordResetOtp(VerifyOtpCommand command);
}
