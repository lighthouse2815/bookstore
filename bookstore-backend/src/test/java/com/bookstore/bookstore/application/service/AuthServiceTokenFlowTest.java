package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.LoginCommand;
import com.bookstore.bookstore.application.command.LogoutCommand;
import com.bookstore.bookstore.application.command.RefreshAccessTokenCommand;
import com.bookstore.bookstore.application.command.ResetPasswordCommand;
import com.bookstore.bookstore.application.command.VerifyOtpCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IOtpService;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.port.out.IJwtService;
import com.bookstore.bookstore.application.port.out.IPasswordResetTokenRepository;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IRefreshTokenRepository;
import com.bookstore.bookstore.application.port.out.IRoleRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.LoginResult;
import com.bookstore.bookstore.application.result.PasswordResetTokenResult;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.PasswordResetToken;
import com.bookstore.bookstore.domain.model.RefreshToken;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTokenFlowTest {

    @Mock
    private IUserService userService;

    @Mock
    private IProfileService profileService;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private IOtpService otpService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IPasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private IPasswordEncoder passwordEncoder;

    @Mock
    private IJwtService jwtService;

    @Mock
    private IRefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_returnsAccessAndRefreshToken() {
        User user = user();
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(eq(user), any(UUID.class))).thenReturn("access-token");
        when(jwtService.calculateRefreshTokenExpiresAt(any(Instant.class)))
                .thenAnswer(invocation -> ((Instant) invocation.getArgument(0)).plus(30, ChronoUnit.DAYS));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResult result = authService.login(new LoginCommand("username", "password"));

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertEquals(user.getId(), captor.getValue().getUserId());
        assertFalse(captor.getValue().isRevoked());
        assertEquals("access-token", result.accessToken());
        assertNotNull(result.refreshToken());
        assertNotEquals(result.refreshToken(), captor.getValue().getTokenHash());
        assertEquals(hashToken(result.refreshToken()), captor.getValue().getTokenHash());
    }

    @Test
    void refresh_rotatesRefreshToken() {
        User user = user();
        RefreshToken currentRefreshToken = new RefreshToken(
                UUID.randomUUID(),
                user.getId(),
                hashToken("old-refresh-token"),
                Instant.now().plus(1, ChronoUnit.DAYS),
                false,
                Instant.now().minus(1, ChronoUnit.DAYS)
        );

        when(refreshTokenRepository.findByTokenHash(hashToken("old-refresh-token"))).thenReturn(Optional.of(currentRefreshToken));
        when(userRepository.findByIdIncludingDeletedForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByTokenHashForUpdate(hashToken("old-refresh-token"))).thenReturn(Optional.of(currentRefreshToken));
        when(jwtService.generateAccessToken(eq(user), any(UUID.class))).thenReturn("new-access-token");
        when(jwtService.calculateRefreshTokenExpiresAt(any(Instant.class)))
                .thenAnswer(invocation -> ((Instant) invocation.getArgument(0)).plus(30, ChronoUnit.DAYS));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginResult result = authService.refresh(new RefreshAccessTokenCommand("old-refresh-token"));

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertTrue(captor.getAllValues().get(0).isRevoked());
        assertEquals(hashToken("old-refresh-token"), captor.getAllValues().get(0).getTokenHash());
        assertNotEquals("old-refresh-token", result.refreshToken());
        assertEquals("new-access-token", result.accessToken());
    }

    @Test
    void refresh_whenTokenExpired_rejectsUnauthorized() {
        User user = user();
        RefreshToken currentRefreshToken = new RefreshToken(
                UUID.randomUUID(),
                user.getId(),
                hashToken("expired-refresh-token"),
                Instant.now().minus(1, ChronoUnit.DAYS),
                false,
                Instant.now().minus(30, ChronoUnit.DAYS)
        );

        when(refreshTokenRepository.findByTokenHash(hashToken("expired-refresh-token"))).thenReturn(Optional.of(currentRefreshToken));
        when(userRepository.findByIdIncludingDeletedForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByTokenHashForUpdate(hashToken("expired-refresh-token"))).thenReturn(Optional.of(currentRefreshToken));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> authService.refresh(new RefreshAccessTokenCommand("expired-refresh-token"))
        );

        assertEquals(ApplicationErrorCode.AUTH_SESSION_EXPIRED, exception.getErrorCode());
    }

    @Test
    void logout_revokesRefreshTokenWhenFound() {
        User user = user();
        RefreshToken currentRefreshToken = new RefreshToken(
                UUID.randomUUID(),
                user.getId(),
                hashToken("logout-refresh-token"),
                Instant.now().plus(1, ChronoUnit.DAYS),
                false,
                Instant.now().minus(1, ChronoUnit.DAYS)
        );

        when(refreshTokenRepository.findByTokenHash(hashToken("logout-refresh-token"))).thenReturn(Optional.of(currentRefreshToken));
        when(refreshTokenRepository.findByTokenHashForUpdate(hashToken("logout-refresh-token"))).thenReturn(Optional.of(currentRefreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.logout(new LogoutCommand("logout-refresh-token"));

        verify(refreshTokenRepository).save(currentRefreshToken);
        assertTrue(currentRefreshToken.isRevoked());
    }

    @Test
    void verifyPasswordResetOtp_returnsOneTimeResetToken() {
        User user = user();
        when(otpService.verifyPasswordResetOtp(any())).thenReturn(user);
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetTokenResult result = authService.verifyPasswordResetOtp(
                new VerifyOtpCommand("username@gmail.com", "123456")
        );

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).markUnusedByUserIdAsUsed(eq(user.getId()), any(Instant.class));
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        assertNotNull(result.resetToken());
        assertNotNull(result.expiresAt());
        assertNotEquals(result.resetToken(), tokenCaptor.getValue().getTokenHash());
    }

    @Test
    void resetPassword_updatesHashMarksTokenUsedAndRevokesRefreshTokens() {
        User user = user();
        PasswordResetToken passwordResetToken = new PasswordResetToken(
                UUID.randomUUID(),
                user.getId(),
                "hashed-reset-token",
                Instant.now().plus(10, ChronoUnit.MINUTES),
                null,
                Instant.now().minus(1, ChronoUnit.MINUTES)
        );
        when(passwordResetTokenRepository.findByTokenHashForUpdate(any(String.class))).thenReturn(Optional.of(passwordResetToken));
        when(userRepository.findByIdIncludingDeletedForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("new-password")).thenReturn("hashed-new-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword(new ResetPasswordCommand("raw-reset-token", "new-password"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        verify(userRepository).save(userCaptor.capture());
        verify(refreshTokenRepository).revokeAllByUserId(
                eq(user.getId()), any(Instant.class), eq(com.bookstore.bookstore.domain.enums.RefreshTokenRevokeReason.PASSWORD_RESET)
        );
        assertTrue(tokenCaptor.getValue().isUsed());
        assertEquals("hashed-new-password", userCaptor.getValue().getPasswordHash());
    }

    @Test
    void revokeSession_whenSessionBelongsToAnotherUser_rejectsUnauthorized() {
        User currentUser = user();
        RefreshToken otherUsersToken = new RefreshToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                hashToken("other-users-refresh-token"),
                Instant.now().plus(1, ChronoUnit.DAYS),
                false,
                Instant.now().minus(1, ChronoUnit.DAYS)
        );
        when(userRepository.findByIdIncludingDeletedForUpdate(currentUser.getId()))
                .thenReturn(Optional.of(currentUser));
        when(refreshTokenRepository.findByIdForUpdate(otherUsersToken.getId()))
                .thenReturn(Optional.of(otherUsersToken));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> authService.revokeSession(
                        currentUser.getId(),
                        otherUsersToken.getId(),
                        UUID.randomUUID()
                )
        );

        assertEquals(ApplicationErrorCode.AUTH_SESSION_REVOKED, exception.getErrorCode());
        verify(refreshTokenRepository, never()).save(otherUsersToken);
    }

    private static User user() {
        Instant now = Instant.EPOCH;
        Role role = new Role(
                UUID.randomUUID(),
                "USER",
                "User role",
                Set.of(),
                now,
                now,
                null
        );

        return new User(
                UUID.randomUUID(),
                "username",
                "password-hash",
                "0123456789",
                "username@gmail.com",
                UserStatus.ACTIVE,
                false,
                Set.of(role),
                now,
                now,
                null
        );
    }

    private static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash token in test", exception);
        }
    }
}
