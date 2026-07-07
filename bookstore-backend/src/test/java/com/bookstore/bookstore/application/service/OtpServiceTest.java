package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.VerifyOtpCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.exception.OtpRateLimitException;
import com.bookstore.bookstore.application.port.out.IEmailSender;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IUserOtpRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.enums.OtpPurpose;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.domain.model.UserOtp;
import com.bookstore.bookstore.infrastructure.email.OtpProperties;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    private static final long OTP_EXPIRATION_MINUTES = 10L;
    private static final long OTP_RESEND_COOLDOWN_SECONDS = 60L;
    private static final long OTP_RESEND_MAX_REQUESTS_PER_WINDOW = 5L;
    private static final long OTP_RESEND_WINDOW_MINUTES = 15L;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IUserOtpRepository userOtpRepository;

    @Mock
    private IPasswordEncoder passwordEncoder;

    @Mock
    private IEmailSender emailSender;

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(
                userRepository,
                userOtpRepository,
                passwordEncoder,
                emailSender,
                new OtpProperties(
                        OTP_EXPIRATION_MINUTES,
                        OTP_RESEND_COOLDOWN_SECONDS,
                        OTP_RESEND_MAX_REQUESTS_PER_WINDOW,
                        OTP_RESEND_WINDOW_MINUTES
                )
        );
    }

    @Test
    void sendRegistrationOtp_savesHashedOtpAndSendsEmail() {
        User user = inactiveUser();
        when(userOtpRepository.findLatestByUserIdAndPurpose(user.getId(), OtpPurpose.REGISTRATION))
                .thenReturn(java.util.Optional.empty());
        when(userOtpRepository.countByUserIdAndPurposeCreatedAfter(eq(user.getId()), eq(OtpPurpose.REGISTRATION), any(Instant.class)))
                .thenReturn(0L);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashed-otp");
        when(userOtpRepository.save(any(UserOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        otpService.sendRegistrationOtp(user);

        ArgumentCaptor<String> rawOtpCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserOtp> savedOtpCaptor = ArgumentCaptor.forClass(UserOtp.class);
        verify(passwordEncoder).encode(rawOtpCaptor.capture());
        verify(userOtpRepository).invalidatePendingByUserIdAndPurpose(
                eq(user.getId()),
                eq(OtpPurpose.REGISTRATION),
                any(Instant.class)
        );
        verify(userOtpRepository).save(savedOtpCaptor.capture());
        verify(emailSender).sendOtpEmail(eq(user.getEmail()), eq(rawOtpCaptor.getValue()), eq(OTP_EXPIRATION_MINUTES));

        UserOtp savedOtp = savedOtpCaptor.getValue();
        assertEquals(user.getId(), savedOtp.getUserId());
        assertEquals(OtpPurpose.REGISTRATION, savedOtp.getPurpose());
        assertEquals("hashed-otp", savedOtp.getOtpHash());
        assertNull(savedOtp.getVerifiedAt());
        assertNull(savedOtp.getInvalidatedAt());
        assertEquals(6, rawOtpCaptor.getValue().length());
    }

    @Test
    void sendPasswordResetOtp_invalidatesActiveOtpsAndSendsResetEmail() {
        User user = activeUser();
        when(userOtpRepository.findLatestByUserIdAndPurpose(user.getId(), OtpPurpose.PASSWORD_RESET))
                .thenReturn(java.util.Optional.empty());
        when(userOtpRepository.countByUserIdAndPurposeCreatedAfter(eq(user.getId()), eq(OtpPurpose.PASSWORD_RESET), any(Instant.class)))
                .thenReturn(0L);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashed-otp");
        when(userOtpRepository.save(any(UserOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        otpService.sendPasswordResetOtp(user);

        ArgumentCaptor<String> rawOtpCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(rawOtpCaptor.capture());
        verify(userOtpRepository).invalidateActiveByUserIdAndPurpose(
                eq(user.getId()),
                eq(OtpPurpose.PASSWORD_RESET),
                any(Instant.class)
        );
        verify(emailSender).sendPasswordResetOtpEmail(eq(user.getEmail()), eq(rawOtpCaptor.getValue()), eq(OTP_EXPIRATION_MINUTES));
        verify(emailSender, never()).sendOtpEmail(eq(user.getEmail()), eq(rawOtpCaptor.getValue()), anyLong());
    }

    @Test
    void sendRegistrationOtp_rejectsResendWithinCooldown() {
        User user = inactiveUser();
        UserOtp latestOtp = pendingOtp(user.getId(), Instant.now().plusSeconds(120), Instant.now().minusSeconds(30));
        when(userOtpRepository.findLatestByUserIdAndPurpose(user.getId(), OtpPurpose.REGISTRATION))
                .thenReturn(java.util.Optional.of(latestOtp));
        when(userOtpRepository.countByUserIdAndPurposeCreatedAfter(eq(user.getId()), eq(OtpPurpose.REGISTRATION), any(Instant.class)))
                .thenReturn(1L);

        OtpRateLimitException exception = assertThrows(OtpRateLimitException.class, () ->
                otpService.sendRegistrationOtp(user)
        );

        verify(userOtpRepository, never()).save(any(UserOtp.class));
        verify(emailSender, never()).sendOtpEmail(any(String.class), any(String.class), anyLong());
        assertTrue(exception.getRetryAfterSeconds() > 0 && exception.getRetryAfterSeconds() <= OTP_RESEND_COOLDOWN_SECONDS);
    }

    @Test
    void sendRegistrationOtp_rejectsWhenTooManyRequestsInWindow() {
        User user = inactiveUser();
        Instant now = Instant.now();
        UserOtp latestOtp = pendingOtp(user.getId(), Instant.now().plusSeconds(120), now.minusSeconds(70));
        UserOtp oldestInWindow = pendingOtp(user.getId(), Instant.now().plusSeconds(120), now.minusSeconds(5 * 60));
        when(userOtpRepository.findLatestByUserIdAndPurpose(user.getId(), OtpPurpose.REGISTRATION))
                .thenReturn(java.util.Optional.of(latestOtp));
        when(userOtpRepository.countByUserIdAndPurposeCreatedAfter(eq(user.getId()), eq(OtpPurpose.REGISTRATION), any(Instant.class)))
                .thenReturn(OTP_RESEND_MAX_REQUESTS_PER_WINDOW);
        when(userOtpRepository.findOldestByUserIdAndPurposeCreatedAfter(eq(user.getId()), eq(OtpPurpose.REGISTRATION), any(Instant.class)))
                .thenReturn(java.util.Optional.of(oldestInWindow));

        OtpRateLimitException exception = assertThrows(OtpRateLimitException.class, () ->
                otpService.sendRegistrationOtp(user)
        );

        verify(userOtpRepository, never()).save(any(UserOtp.class));
        verify(emailSender, never()).sendOtpEmail(any(String.class), any(String.class), anyLong());
        assertTrue(exception.getRetryAfterSeconds() >= 1);
        assertTrue(exception.getRetryAfterSeconds() <= OTP_RESEND_WINDOW_MINUTES * 60);
    }

    @Test
    void verifyRegistrationOtp_activatesUserWhenOtpIsValid() {
        User user = inactiveUser();
        UserOtp userOtp = pendingOtp(user.getId(), Instant.now().plusSeconds(120));

        when(userRepository.findByEmailIncludingDeleted(user.getEmail())).thenReturn(java.util.Optional.of(user));
        when(userOtpRepository.findLatestPendingByUserIdAndPurpose(user.getId(), OtpPurpose.REGISTRATION))
                .thenReturn(java.util.Optional.of(userOtp));
        when(passwordEncoder.matches("123456", userOtp.getOtpHash())).thenReturn(true);
        when(userOtpRepository.save(any(UserOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        otpService.verifyRegistrationOtp(new VerifyOtpCommand(user.getEmail(), "123456"));

        ArgumentCaptor<UserOtp> verifiedOtpCaptor = ArgumentCaptor.forClass(UserOtp.class);
        ArgumentCaptor<User> activatedUserCaptor = ArgumentCaptor.forClass(User.class);
        verify(userOtpRepository).save(verifiedOtpCaptor.capture());
        verify(userRepository).save(activatedUserCaptor.capture());

        assertNotNull(verifiedOtpCaptor.getValue().getVerifiedAt());
        assertEquals(UserStatus.ACTIVE, activatedUserCaptor.getValue().getStatus());
    }

    @Test
    void verifyRegistrationOtp_invalidatesExpiredOtp() {
        User user = inactiveUser();
        UserOtp expiredOtp = pendingOtp(user.getId(), Instant.now().minusSeconds(1));

        when(userRepository.findByEmailIncludingDeleted(user.getEmail())).thenReturn(java.util.Optional.of(user));
        when(userOtpRepository.findLatestPendingByUserIdAndPurpose(user.getId(), OtpPurpose.REGISTRATION))
                .thenReturn(java.util.Optional.of(expiredOtp));
        when(userOtpRepository.save(any(UserOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                otpService.verifyRegistrationOtp(new VerifyOtpCommand(user.getEmail(), "123456"))
        );

        ArgumentCaptor<UserOtp> invalidatedOtpCaptor = ArgumentCaptor.forClass(UserOtp.class);
        verify(userOtpRepository).save(invalidatedOtpCaptor.capture());
        verify(userRepository, never()).save(any(User.class));

        assertEquals(ApplicationErrorCode.OTP_EXPIRED, exception.getErrorCode());
        assertNotNull(invalidatedOtpCaptor.getValue().getInvalidatedAt());
    }

    @Test
    void verifyRegistrationOtp_rejectsInvalidOtpCode() {
        User user = inactiveUser();
        UserOtp userOtp = pendingOtp(user.getId(), Instant.now().plusSeconds(120));

        when(userRepository.findByEmailIncludingDeleted(user.getEmail())).thenReturn(java.util.Optional.of(user));
        when(userOtpRepository.findLatestPendingByUserIdAndPurpose(user.getId(), OtpPurpose.REGISTRATION))
                .thenReturn(java.util.Optional.of(userOtp));
        when(passwordEncoder.matches("000000", userOtp.getOtpHash())).thenReturn(false);

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                otpService.verifyRegistrationOtp(new VerifyOtpCommand(user.getEmail(), "000000"))
        );

        verify(userOtpRepository, never()).save(any(UserOtp.class));
        verify(userRepository, never()).save(any(User.class));
        assertEquals(ApplicationErrorCode.OTP_INVALID, exception.getErrorCode());
    }

    @Test
    void verifyPasswordResetOtp_marksOtpVerifiedAndReturnsUserWithoutUpdatingUser() {
        User user = activeUser();
        UserOtp userOtp = pendingOtp(user.getId(), OtpPurpose.PASSWORD_RESET, Instant.now().plusSeconds(120));

        when(userRepository.findByEmailIncludingDeleted(user.getEmail())).thenReturn(java.util.Optional.of(user));
        when(userOtpRepository.findLatestPendingByUserIdAndPurpose(user.getId(), OtpPurpose.PASSWORD_RESET))
                .thenReturn(java.util.Optional.of(userOtp));
        when(passwordEncoder.matches("123456", userOtp.getOtpHash())).thenReturn(true);
        when(userOtpRepository.save(any(UserOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = otpService.verifyPasswordResetOtp(new VerifyOtpCommand(user.getEmail(), "123456"));

        ArgumentCaptor<UserOtp> verifiedOtpCaptor = ArgumentCaptor.forClass(UserOtp.class);
        verify(userOtpRepository).save(verifiedOtpCaptor.capture());
        verify(userRepository, never()).save(any(User.class));
        assertEquals(user.getId(), result.getId());
        assertNotNull(verifiedOtpCaptor.getValue().getVerifiedAt());
    }

    private static User inactiveUser() {
        Instant now = Instant.now().minusSeconds(60);
        return new User(
                UUID.randomUUID(),
                "test@gmail.com",
                "hashed-password",
                null,
                "test@gmail.com",
                UserStatus.INACTIVE,
                false,
                Set.of(new Role(
                        UUID.randomUUID(),
                        "USER",
                        "Default user role",
                        Set.of(),
                        now,
                        now,
                        null
                )),
                now,
                now,
                null
        );
    }

    private static User activeUser() {
        Instant now = Instant.now().minusSeconds(60);
        return new User(
                UUID.randomUUID(),
                "test@gmail.com",
                "hashed-password",
                null,
                "test@gmail.com",
                UserStatus.ACTIVE,
                false,
                Set.of(new Role(
                        UUID.randomUUID(),
                        "USER",
                        "Default user role",
                        Set.of(),
                        now,
                        now,
                        null
                )),
                now,
                now,
                null
        );
    }

    private static UserOtp pendingOtp(UUID userId, Instant expiresAt) {
        return pendingOtp(userId, OtpPurpose.REGISTRATION, expiresAt);
    }

    private static UserOtp pendingOtp(UUID userId, OtpPurpose purpose, Instant expiresAt) {
        return pendingOtp(userId, purpose, expiresAt, Instant.now().minusSeconds(30));
    }

    private static UserOtp pendingOtp(UUID userId, Instant expiresAt, Instant createdAt) {
        return pendingOtp(userId, OtpPurpose.REGISTRATION, expiresAt, createdAt);
    }

    private static UserOtp pendingOtp(UUID userId, OtpPurpose purpose, Instant expiresAt, Instant createdAt) {
        return new UserOtp(
                UUID.randomUUID(),
                userId,
                purpose,
                "hashed-otp",
                expiresAt,
                null,
                null,
                createdAt,
                createdAt
        );
    }
}
