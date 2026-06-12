package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.RequestRegistrationOtpCommand;
import com.bookstore.bookstore.application.command.VerifyOtpCommand;
import com.bookstore.bookstore.presentation.request.RequestRegistrationOtpRequest;
import com.bookstore.bookstore.presentation.request.VerifyOtpRequest;
import org.springframework.stereotype.Component;

@Component
public class OtpWebMapper {

    public RequestRegistrationOtpCommand toRequestRegistrationOtpCommand(RequestRegistrationOtpRequest request) {
        return new RequestRegistrationOtpCommand(request.email());
    }

    public VerifyOtpCommand toVerifyOtpCommand(VerifyOtpRequest request) {
        return new VerifyOtpCommand(request.email(), request.otpCode());
    }
}
