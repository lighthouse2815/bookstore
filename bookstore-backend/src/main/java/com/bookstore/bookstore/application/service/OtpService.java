package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.RequestRegistrationOtpCommand;
import com.bookstore.bookstore.application.exception.OtpRateLimitException;
import com.bookstore.bookstore.application.command.VerifyOtpCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IOtpService;
import com.bookstore.bookstore.application.port.in.IAuditLogService;
import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.application.port.out.IEmailSender;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IOtpSettings;
import com.bookstore.bookstore.application.port.out.IUserOtpRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.enums.OtpPurpose;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.domain.model.UserOtp;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService implements IOtpService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final long DEFAULT_EXPIRATION_MINUTES = 10;
    private static final long DEFAULT_MAX_ATTEMPTS = 5;
    private static final long DEFAULT_RESEND_COOLDOWN_SECONDS = 60;
    private static final long DEFAULT_RESEND_MAX_REQUESTS_PER_WINDOW = 5;
    private static final long DEFAULT_RESEND_WINDOW_MINUTES = 15;

    private final IUserRepository userRepository;
    private final IUserOtpRepository userOtpRepository;
    private final IPasswordEncoder passwordEncoder;
    private final IEmailSender emailSender;
    private final IOtpSettings otpSettings;
    private final IAuditLogService auditLogService;

    public OtpService(
            IUserRepository userRepository,
            IUserOtpRepository userOtpRepository,
            IPasswordEncoder passwordEncoder,
            IEmailSender emailSender,
            IOtpSettings otpSettings
    ) {
        this(userRepository, userOtpRepository, passwordEncoder, emailSender, otpSettings, null);
    }

    @Autowired
    public OtpService(
            IUserRepository userRepository,
            IUserOtpRepository userOtpRepository,
            IPasswordEncoder passwordEncoder,
            IEmailSender emailSender,
            IOtpSettings otpSettings,
            IAuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.userOtpRepository = userOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.otpSettings = otpSettings;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestRegistrationOtp(RequestRegistrationOtpCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String email = StringUtils.trimToNull(command.email());
        if (email == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "email");
        }

        userRepository.findByEmailIncludingDeleted(email)
                .filter(this::canRequestRegistrationOtp)
                .ifPresent(this::sendRegistrationOtp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendRegistrationOtp(User user) {
        if (user == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "user");
        }

        sendOtp(user, OtpPurpose.REGISTRATION);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendPasswordResetOtp(User user) {
        if (user == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "user");
        }

        sendOtp(user, OtpPurpose.PASSWORD_RESET);
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
        verifyOtp(user, otpCode, OtpPurpose.REGISTRATION);
        user.activate();
        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User verifyPasswordResetOtp(VerifyOtpCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String email = StringUtils.trimToNull(command.email());
        String otpCode = StringUtils.trimToNull(command.otpCode());
        if (email == null || otpCode == null) {
            throw new ApplicationException(ApplicationErrorCode.OTP_INVALID);
        }

        User user = loadPasswordResetUser(email, ApplicationErrorCode.OTP_INVALID);
        verifyOtp(user, otpCode, OtpPurpose.PASSWORD_RESET);
        return user;
    }

    private void verifyOtp(User user, String otpCode, OtpPurpose purpose) {
        UserOtp userOtp = userOtpRepository.findLatestPendingByUserIdAndPurposeForUpdate(user.getId(), purpose)
                .or(() -> userOtpRepository.findLatestPendingByUserIdAndPurpose(user.getId(), purpose))
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.OTP_INVALID));

        Instant now = Instant.now();
        if (userOtp.isExpiredAt(now)) {
            userOtp.invalidate(now);
            userOtpRepository.save(userOtp);
            throw new ApplicationException(ApplicationErrorCode.OTP_EXPIRED);
        }

        if (!passwordEncoder.matches(otpCode, userOtp.getOtpHash())) {
            userOtp.recordFailedAttempt(now);
            userOtpRepository.save(userOtp);
            if (userOtp.isAttemptLimitReached()) {
                audit(user, "OTP_LOCKED");
                throw new ApplicationException(ApplicationErrorCode.OTP_LOCKED);
            }
            audit(user, "OTP_FAILED");
            throw new ApplicationException(ApplicationErrorCode.OTP_INVALID);
        }

        userOtp.markVerified(now);
        userOtpRepository.save(userOtp);
        audit(user, "OTP_VERIFIED");
    }

    private void sendOtp(User user, OtpPurpose purpose) {
        Instant now = Instant.now();
        enforceRateLimit(user, purpose, now);
        String rawOtp = generateOtpCode(OTP_LENGTH);

        if (purpose == OtpPurpose.PASSWORD_RESET) {
            userOtpRepository.invalidateActiveByUserIdAndPurpose(user.getId(), purpose, now);
        } else {
            userOtpRepository.invalidatePendingByUserIdAndPurpose(user.getId(), purpose, now);
        }

        userOtpRepository.save(new UserOtp(
                UUID.randomUUID(),
                user.getId(),
                purpose,
                passwordEncoder.encode(rawOtp),
                0,
                (int) resolveMaxAttempts(),
                null,
                now.plusSeconds(resolveExpirationMinutes() * 60),
                null,
                null,
                now,
                now
        ));

        if (purpose == OtpPurpose.PASSWORD_RESET) {
            emailSender.sendPasswordResetOtpEmail(user.getEmail(), rawOtp, resolveExpirationMinutes());
            return;
        }

        emailSender.sendOtpEmail(user.getEmail(), rawOtp, resolveExpirationMinutes());
    }

    private void enforceRateLimit(User user, OtpPurpose purpose, Instant now) {
        long retryAfterSeconds = 0;

        UserOtp latestOtp = userOtpRepository.findLatestByUserIdAndPurpose(user.getId(), purpose).orElse(null);
        if (latestOtp != null) {
            retryAfterSeconds = Math.max(
                    retryAfterSeconds,
                    calculateRetryAfterSeconds(
                            latestOtp.getCreatedAt().plusSeconds(resolveResendCooldownSeconds()),
                            now
                    )
            );
        }

        Instant windowStart = now.minusSeconds(resolveResendWindowMinutes() * 60);
        long recentOtpCount = userOtpRepository.countByUserIdAndPurposeCreatedAfter(user.getId(), purpose, windowStart);
        if (recentOtpCount >= resolveResendMaxRequestsPerWindow()) {
            UserOtp oldestOtpInWindow = userOtpRepository
                    .findOldestByUserIdAndPurposeCreatedAfter(user.getId(), purpose, windowStart)
                    .orElse(null);
            if (oldestOtpInWindow != null) {
                retryAfterSeconds = Math.max(
                        retryAfterSeconds,
                        calculateRetryAfterSeconds(
                                oldestOtpInWindow.getCreatedAt().plusSeconds(resolveResendWindowMinutes() * 60),
                                now
                        )
                );
            }
        }

        if (retryAfterSeconds > 0) {
            throw new OtpRateLimitException(retryAfterSeconds);
        }
    }

    private User loadPasswordResetUser(String email, ApplicationErrorCode errorCode) {
        User user = userRepository.findByEmailIncludingDeleted(email)
                .orElseThrow(() -> new ApplicationException(errorCode));
        try {
            user.requireCanLogin();
        } catch (DomainException exception) {
            throw new ApplicationException(errorCode);
        }
        return user;
    }

    private boolean canRequestRegistrationOtp(User user) {
        return user.getDeletedAt() == null
                && !user.isLocked()
                && user.getStatus() == UserStatus.INACTIVE;
    }

    private long resolveExpirationMinutes() {
        return otpSettings.expirationMinutes() > 0
                ? otpSettings.expirationMinutes()
                : DEFAULT_EXPIRATION_MINUTES;
    }

    private long resolveResendCooldownSeconds() {
        return otpSettings.resendCooldownSeconds() > 0
                ? otpSettings.resendCooldownSeconds()
                : DEFAULT_RESEND_COOLDOWN_SECONDS;
    }

    private long resolveMaxAttempts() {
        return otpSettings.maxAttempts() > 0 ? otpSettings.maxAttempts() : DEFAULT_MAX_ATTEMPTS;
    }

    private long resolveResendMaxRequestsPerWindow() {
        return otpSettings.resendMaxRequestsPerWindow() > 0
                ? otpSettings.resendMaxRequestsPerWindow()
                : DEFAULT_RESEND_MAX_REQUESTS_PER_WINDOW;
    }

    private long resolveResendWindowMinutes() {
        return otpSettings.resendWindowMinutes() > 0
                ? otpSettings.resendWindowMinutes()
                : DEFAULT_RESEND_WINDOW_MINUTES;
    }

    private long calculateRetryAfterSeconds(Instant allowedAt, Instant now) {
        if (!allowedAt.isAfter(now)) {
            return 0;
        }

        long millis = Duration.between(now, allowedAt).toMillis();
        return Math.max(1, (millis + 999) / 1000);
    }

    private String generateOtpCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(SECURE_RANDOM.nextInt(10));
        }
        return builder.toString();
    }

    private void audit(User user, String action) {
        if (auditLogService == null) {
            return;
        }
        auditLogService.record(new AuditLogCommand(
                user.getId(), user.getUsername(), null, action, AuditTargetType.USER_OTP, null, action,
                null, null, null, null, Instant.now()
        ));
    }
}
