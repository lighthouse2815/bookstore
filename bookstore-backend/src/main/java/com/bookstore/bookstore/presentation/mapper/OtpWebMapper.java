package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.VerifyOtpCommand;
import com.bookstore.bookstore.presentation.request.VerifyOtpRequest;
import org.springframework.stereotype.Component;

@Component
public class OtpWebMapper {

    public VerifyOtpCommand toVerifyOtpCommand(VerifyOtpRequest request) {
        return new VerifyOtpCommand(request.email(), request.otpCode());
    }
}
