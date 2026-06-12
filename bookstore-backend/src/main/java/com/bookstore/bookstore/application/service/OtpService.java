package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.VerifyOtpCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IOtpService;
import com.bookstore.bookstore.application.port.out.IEmailSender;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IUserOtpRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.domain.model.UserOtp;
import com.bookstore.bookstore.infrastructure.email.OtpProperties;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OtpService implements IOtpService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final long DEFAULT_EXPIRATION_MINUTES = 10;

    private final IUserRepository userRepository;
    private final IUserOtpRepository userOtpRepository;
    private final IPasswordEncoder passwordEncoder;
    private final IEmailSender emailSender;
    private final OtpProperties otpProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendRegistrationOtp(User user) {
        if (user == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "user");
        }

        Instant now = Instant.now();
        String rawOtp = generateOtpCode(resolveOtpLength());

        userOtpRepository.invalidatePendingByUserId(user.getId(), now);
        userOtpRepository.save(new UserOtp(
                UUID.randomUUID(),
                user.getId(),
                passwordEncoder.encode(rawOtp),
                now.plusSeconds(resolveExpirationMinutes() * 60),
                null,
                null,
                now,
                now
        ));

        emailSender.sendOtpEmail(user.getEmail(), rawOtp, resolveExpirationMinutes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyRegistrationOtp(VerifyOtpCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String email = StringUtils.trimToNull(command.email());
        String otpCode = StringUtils.trimToNull(command.otpCode());
        if (email == null || otpCode == null) {
            throw new ApplicationException(ApplicationErrorCode.OTP_INVALID);
        }

        User user = userRepository.findByEmailIncludingDeleted(email)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.OTP_INVALID));
        UserOtp userOtp = userOtpRepository.findLatestPendingByUserId(user.getId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.OTP_INVALID));

        Instant now = Instant.now();
        if (userOtp.isExpiredAt(now)) {
            userOtp.invalidate(now);
            userOtpRepository.save(userOtp);
            throw new ApplicationException(ApplicationErrorCode.OTP_EXPIRED);
        }

        if (!passwordEncoder.matches(otpCode, userOtp.getOtpHash())) {
            throw new ApplicationException(ApplicationErrorCode.OTP_INVALID);
        }

        userOtp.markVerified(now);
        userOtpRepository.save(userOtp);
        user.activate();
        userRepository.save(user);
    }

    private int resolveOtpLength() {
        return OTP_LENGTH;
    }

    private long resolveExpirationMinutes() {
        return otpProperties.expirationMinutes() > 0
                ? otpProperties.expirationMinutes()
                : DEFAULT_EXPIRATION_MINUTES;
    }

    private String generateOtpCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(SECURE_RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
