package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.GoogleLoginCommand;
import com.bookstore.bookstore.application.command.LoginCommand;
import com.bookstore.bookstore.application.command.RegisterCommand;
import com.bookstore.bookstore.application.command.RequestPasswordResetOtpCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.exception.GoogleIdTokenVerificationException;
import com.bookstore.bookstore.application.port.in.IOtpService;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.port.in.IAuditLogService;
import com.bookstore.bookstore.application.port.out.IGoogleIdTokenVerifier;
import com.bookstore.bookstore.application.port.out.IJwtService;
import com.bookstore.bookstore.application.port.out.IPasswordResetTokenRepository;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IRefreshTokenRepository;
import com.bookstore.bookstore.application.port.out.IRoleRepository;
import com.bookstore.bookstore.application.port.out.IUserAuthIdentityRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.port.out.VerifiedGoogleIdToken;
import com.bookstore.bookstore.application.result.RegisterResult;
import com.bookstore.bookstore.domain.enums.AuthProvider;
import com.bookstore.bookstore.domain.enums.RefreshTokenRevokeReason;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.RefreshToken;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.domain.model.UserAuthIdentity;
import java.time.Instant;
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
class AuthServiceTest {

    private static final String USER_ROLE = "USER";

    @Mock
    private IUserService userService;

    @Mock
    private IProfileService profileService;

    @Mock
    private IOtpService otpService;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IUserAuthIdentityRepository userAuthIdentityRepository;

    @Mock
    private IPasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private IPasswordEncoder passwordEncoder;

    @Mock
    private IJwtService jwtService;

    @Mock
    private IRefreshTokenRepository refreshTokenRepository;

    @Mock
    private IGoogleIdTokenVerifier googleIdTokenVerifier;

    @Mock
    private IAuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_doesNotTrimPassword() {
        RegisterCommand command = new RegisterCommand(
                "test@gmail.com",
                "  secret  "
        );

        when(userService.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileService.create(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findByNameActive(USER_ROLE)).thenReturn(Optional.of(defaultRole()));
        when(passwordEncoder.encode("  secret  ")).thenReturn("hashed-secret");

        RegisterResult result = authService.register(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        verify(userService).create(userCaptor.capture());
        verify(profileService).create(profileCaptor.capture());
        verify(otpService).sendRegistrationOtp(any(User.class));
        verify(passwordEncoder).encode("  secret  ");

        assertEquals("hashed-secret", userCaptor.getValue().getPasswordHash());
        assertEquals("test@gmail.com", userCaptor.getValue().getUsername());
        assertEquals("test@gmail.com", userCaptor.getValue().getEmail());
        assertNull(userCaptor.getValue().getPhoneNumber());
        assertNull(profileCaptor.getValue().getFirstName());
        assertNull(profileCaptor.getValue().getLastName());
        assertNull(profileCaptor.getValue().getAvatarUrl());
        assertNull(profileCaptor.getValue().getGender());
        assertNull(profileCaptor.getValue().getDateOfBirth());
        assertEquals("test@gmail.com", result.username());
    }

    @Test
    void register_setsUsernameFromEmail() {
        RegisterCommand command = new RegisterCommand(
                "test@gmail.com",
                "secret123"
        );

        when(userService.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileService.create(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findByNameActive(USER_ROLE)).thenReturn(Optional.of(defaultRole()));
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");

        authService.register(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).create(userCaptor.capture());
        verify(otpService).sendRegistrationOtp(any(User.class));
        assertEquals("test@gmail.com", userCaptor.getValue().getUsername());
    }

    @Test
    void register_restoresDeletedAccountAsInactiveUserAndRevokesOldSessions() {
        User deletedUser = userWithPassword(
                UserStatus.ACTIVE,
                true,
                Instant.now().minusSeconds(60),
                "old-password"
        );
        Role defaultRole = defaultRole();
        when(roleRepository.findByNameActive(USER_ROLE)).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode("new-password")).thenReturn("new-password-hash");
        when(userRepository.findByEmailIncludingDeleted("test@gmail.com")).thenReturn(Optional.of(deletedUser));
        when(userRepository.save(deletedUser)).thenReturn(deletedUser);

        RegisterResult result = authService.register(new RegisterCommand("test@gmail.com", "new-password"));

        assertEquals(deletedUser.getUsername(), result.username());
        assertNull(deletedUser.getDeletedAt());
        assertEquals(UserStatus.INACTIVE, deletedUser.getStatus());
        assertEquals("new-password-hash", deletedUser.getPasswordHash());
        assertTrue(!deletedUser.isLocked());
        assertEquals(Set.of(defaultRole), deletedUser.getRoles());
        verify(userService, never()).create(any(User.class));
        verify(profileService).restoreForUser(deletedUser.getId());
        verify(refreshTokenRepository).revokeAllByUserId(
                eq(deletedUser.getId()),
                any(Instant.class),
                eq(RefreshTokenRevokeReason.SESSION_REVOKED)
        );
        verify(otpService).sendRegistrationOtp(deletedUser);
    }

    @Test
    void login_doesNotTrimPassword() {
        String password = "  secret  ";
        User user = activeUserWithPassword(password);
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(eq(user), any(UUID.class))).thenReturn("jwt-token");
        when(jwtService.calculateRefreshTokenExpiresAt(any(Instant.class))).thenReturn(Instant.now().plusSeconds(300));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = authService.login(new LoginCommand("username", password));

        assertEquals(user.getId(), result.userId());
        assertEquals(UserStatus.ACTIVE, result.status());
        assertTrue(result.roles().contains(USER_ROLE));
        assertEquals("jwt-token", result.accessToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_rejectsMissingUser() {
        when(userRepository.findByUsernameActive("missing")).thenReturn(Optional.empty());

        ApplicationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ApplicationException.class,
                () -> authService.login(new LoginCommand("missing", "secret"))
        );

        assertEquals(ApplicationErrorCode.AUTH_INVALID_CREDENTIALS, exception.getErrorCode());
    }

    @Test
    void login_rejectsInactiveUser() {
        User user = userWithPassword(UserStatus.INACTIVE, false, null, "secret");
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.of(user));

        ApplicationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ApplicationException.class,
                () -> authService.login(new LoginCommand("username", "secret"))
        );

        assertEquals(ApplicationErrorCode.AUTH_INVALID_CREDENTIALS, exception.getErrorCode());
    }

    @Test
    void login_rejectsLockedUser() {
        User user = userWithPassword(UserStatus.ACTIVE, true, null, "secret");
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.of(user));

        ApplicationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ApplicationException.class,
                () -> authService.login(new LoginCommand("username", "secret"))
        );

        assertEquals(ApplicationErrorCode.AUTH_INVALID_CREDENTIALS, exception.getErrorCode());
    }

    @Test
    void login_rejectsDeletedUser() {
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.empty());

        ApplicationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ApplicationException.class,
                () -> authService.login(new LoginCommand("username", "secret"))
        );

        assertEquals(ApplicationErrorCode.AUTH_INVALID_CREDENTIALS, exception.getErrorCode());
    }

    @Test
    void login_rejectsInvalidPassword() {
        User user = userWithPassword(UserStatus.ACTIVE, false, null, "correct-password");
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPasswordHash())).thenReturn(false);

        ApplicationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ApplicationException.class,
                () -> authService.login(new LoginCommand("username", "wrong-password"))
        );

        assertEquals(ApplicationErrorCode.AUTH_INVALID_CREDENTIALS, exception.getErrorCode());
    }

    @Test
    void login_rejectsAccountWithoutPassword() {
        User user = userWithoutPassword(UserStatus.ACTIVE, false, null);
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.of(user));

        ApplicationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ApplicationException.class,
                () -> authService.login(new LoginCommand("username", "secret"))
        );

        assertEquals(ApplicationErrorCode.AUTH_INVALID_CREDENTIALS, exception.getErrorCode());
    }

    @Test
    void loginWithGoogle_createsNewUserProfileAndIdentityForFirstLogin() {
        VerifiedGoogleIdToken googleToken = verifiedGoogleIdToken();
        when(googleIdTokenVerifier.verify("google-token")).thenReturn(googleToken);
        when(userAuthIdentityRepository.findByProviderAndProviderSubject(AuthProvider.GOOGLE, googleToken.subject()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIncludingDeleted(googleToken.email())).thenReturn(Optional.empty());
        when(userRepository.existsByUsernameIncludingDeleted(googleToken.email())).thenReturn(false);
        when(userAuthIdentityRepository.findByUserIdAndProvider(any(UUID.class), any(AuthProvider.class)))
                .thenReturn(Optional.empty());
        when(userService.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileService.create(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAuthIdentityRepository.save(any(UserAuthIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findByNameActive(USER_ROLE)).thenReturn(Optional.of(defaultRole()));
        stubTokenIssuance();

        var result = authService.loginWithGoogle(new GoogleLoginCommand("google-token"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
        ArgumentCaptor<UserAuthIdentity> identityCaptor = ArgumentCaptor.forClass(UserAuthIdentity.class);

        verify(userService).create(userCaptor.capture());
        verify(profileService).create(profileCaptor.capture());
        verify(userAuthIdentityRepository).save(identityCaptor.capture());
        assertEquals(UserStatus.ACTIVE, result.status());
        assertEquals("jwt-token", result.accessToken());
        assertNull(userCaptor.getValue().getPasswordHash());
        assertEquals("test@gmail.com", userCaptor.getValue().getEmail());
        assertEquals("First", profileCaptor.getValue().getFirstName());
        assertEquals("Last", profileCaptor.getValue().getLastName());
        assertNull(profileCaptor.getValue().getAvatarUrl());
        assertEquals(AuthProvider.GOOGLE, identityCaptor.getValue().getProvider());
        assertEquals("google-subject-123", identityCaptor.getValue().getProviderSubject());
    }

    @Test
    void loginWithGoogle_linksAndActivatesExistingInactiveUser() {
        VerifiedGoogleIdToken googleToken = verifiedGoogleIdToken();
        User existingUser = userWithPassword(UserStatus.INACTIVE, false, null, "secret");
        when(googleIdTokenVerifier.verify("google-token")).thenReturn(googleToken);
        when(userAuthIdentityRepository.findByProviderAndProviderSubject(AuthProvider.GOOGLE, googleToken.subject()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIncludingDeleted(googleToken.email())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAuthIdentityRepository.findByUserIdAndProvider(existingUser.getId(), AuthProvider.GOOGLE))
                .thenReturn(Optional.empty());
        when(userAuthIdentityRepository.save(any(UserAuthIdentity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        stubTokenIssuance();

        var result = authService.loginWithGoogle(new GoogleLoginCommand("google-token"));

        assertEquals(UserStatus.ACTIVE, result.status());
        verify(userRepository).save(any(User.class));
        verify(userService, never()).create(any(User.class));
        verify(profileService, never()).create(any(Profile.class));
    }

    @Test
    void loginWithGoogle_rejectsInvalidIdToken() {
        when(googleIdTokenVerifier.verify("bad-token"))
                .thenThrow(new GoogleIdTokenVerificationException("invalid"));

        ApplicationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ApplicationException.class,
                () -> authService.loginWithGoogle(new GoogleLoginCommand("bad-token"))
        );

        assertEquals(ApplicationErrorCode.AUTH_GOOGLE_INVALID_ID_TOKEN, exception.getErrorCode());
    }

    @Test
    void requestPasswordResetOtp_sendsOtpForEligibleUser() {
        User user = activeUserWithPassword("secret");
        when(userRepository.findByEmailIncludingDeleted("test@gmail.com")).thenReturn(Optional.of(user));

        authService.requestPasswordResetOtp(new RequestPasswordResetOtpCommand("test@gmail.com"));

        verify(otpService).sendPasswordResetOtp(user);
    }

    @Test
    void requestPasswordResetOtp_ignoresIneligibleUser() {
        User user = userWithPassword(UserStatus.INACTIVE, false, null, "secret");
        when(userRepository.findByEmailIncludingDeleted("test@gmail.com")).thenReturn(Optional.of(user));

        authService.requestPasswordResetOtp(new RequestPasswordResetOtpCommand("test@gmail.com"));

        verify(otpService, never()).sendPasswordResetOtp(any(User.class));
    }

    private static User activeUserWithPassword(String password) {
        return userWithPassword(UserStatus.ACTIVE, false, null, password);
    }

    private void stubTokenIssuance() {
        when(jwtService.generateAccessToken(any(User.class), any(UUID.class))).thenReturn("jwt-token");
        when(jwtService.calculateRefreshTokenExpiresAt(any(Instant.class))).thenReturn(Instant.now().plusSeconds(300));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static VerifiedGoogleIdToken verifiedGoogleIdToken() {
        return new VerifiedGoogleIdToken(
                "google-subject-123",
                "test@gmail.com",
                true,
                "First",
                "Last",
                "https://avatar.example.com/user.png"
        );
    }

    private static Role defaultRole() {
        Instant now = Instant.EPOCH;
        return new Role(
                UUID.randomUUID(),
                USER_ROLE,
                "user role",
                Set.of(),
                now,
                now,
                null
        );
    }

    private static User userWithoutPassword(UserStatus status, boolean locked, Instant deletedAt) {
        Instant now = deletedAt == null ? Instant.now() : deletedAt.minusSeconds(1);
        return new User(
                UUID.randomUUID(),
                "username",
                null,
                "0123456789",
                "test@gmail.com",
                status,
                locked,
                Set.of(new Role(
                        UUID.randomUUID(),
                        USER_ROLE,
                        "Default user role",
                        Set.of(),
                        now,
                        now,
                        null
                )),
                now,
                now,
                deletedAt
        );
    }

    private static User userWithPassword(UserStatus status, boolean locked, Instant deletedAt, String password) {
        Instant now = deletedAt == null ? Instant.now() : deletedAt.minusSeconds(1);
        return new User(
                UUID.randomUUID(),
                "username",
                "stored-" + password,
                "0123456789",
                "test@gmail.com",
                status,
                locked,
                Set.of(new Role(
                        UUID.randomUUID(),
                        USER_ROLE,
                        "Default user role",
                        Set.of(),
                        now,
                        now,
                        null
                )),
                now,
                now,
                deletedAt
        );
    }
}
