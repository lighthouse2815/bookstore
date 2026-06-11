package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.LoginCommand;
import com.bookstore.bookstore.application.command.RegisterCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IOtpService;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.port.out.IJwtService;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IRefreshTokenRepository;
import com.bookstore.bookstore.application.port.out.IRoleRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.RegisterResult;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.RefreshToken;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
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
    private IPasswordEncoder passwordEncoder;

    @Mock
    private IJwtService jwtService;

    @Mock
    private IRefreshTokenRepository refreshTokenRepository;

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
    void login_doesNotTrimPassword() {
        String password = "  secret  ";
        User user = activeUserWithPassword(password);
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("jwt-token");
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

        assertEquals(ApplicationErrorCode.AUTH_USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void login_rejectsInactiveUser() {
        User user = userWithPassword(UserStatus.INACTIVE, false, null, "secret");
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.of(user));

        DomainException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DomainException.class,
                () -> authService.login(new LoginCommand("username", "secret"))
        );

        assertEquals(DomainErrorCode.USER_NOT_ACTIVE_CANNOT_LOGIN, exception.getErrorCode());
    }

    @Test
    void login_rejectsLockedUser() {
        User user = userWithPassword(UserStatus.ACTIVE, true, null, "secret");
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.of(user));

        DomainException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DomainException.class,
                () -> authService.login(new LoginCommand("username", "secret"))
        );

        assertEquals(DomainErrorCode.BLOCKED_USER_CANNOT_LOGIN, exception.getErrorCode());
    }

    @Test
    void login_rejectsDeletedUser() {
        when(userRepository.findByUsernameActive("username")).thenReturn(Optional.empty());

        ApplicationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ApplicationException.class,
                () -> authService.login(new LoginCommand("username", "secret"))
        );

        assertEquals(ApplicationErrorCode.AUTH_USER_NOT_FOUND, exception.getErrorCode());
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

        assertEquals(ApplicationErrorCode.AUTH_INVALID_PASSWORD, exception.getErrorCode());
    }

    private static User activeUserWithPassword(String password) {
        return userWithPassword(UserStatus.ACTIVE, false, null, password);
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
