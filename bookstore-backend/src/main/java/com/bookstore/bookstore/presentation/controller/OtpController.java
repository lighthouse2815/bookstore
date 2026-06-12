package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.IOtpService;
import com.bookstore.bookstore.presentation.mapper.OtpWebMapper;
import com.bookstore.bookstore.presentation.request.RequestRegistrationOtpRequest;
import com.bookstore.bookstore.presentation.request.VerifyOtpRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final IOtpService otpService;
    private final OtpWebMapper otpWebMapper;

    @PostMapping("/request")
    public ApiResponse<Void> requestOtp(@Valid @RequestBody RequestRegistrationOtpRequest request) {
        otpService.requestRegistrationOtp(otpWebMapper.toRequestRegistrationOtpCommand(request));
        return ApiResponse.success("Neu email ton tai va tai khoan chua kich hoat, OTP da duoc gui", null);
    }

    @PostMapping("/verify")
    public ApiResponse<Void> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verifyRegistrationOtp(otpWebMapper.toVerifyOtpCommand(request));
        return ApiResponse.success("Xac thuc OTP thanh cong", null);
    }
}
