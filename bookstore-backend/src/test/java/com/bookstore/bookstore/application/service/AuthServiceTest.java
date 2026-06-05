package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.LoginCommand;
import com.bookstore.bookstore.application.command.RegisterCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.in.IRoleService;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.port.out.IJwtService;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.RegisterResult;
import com.bookstore.bookstore.domain.enums.Gender;
import com.bookstore.bookstore.domain.enums.RoleName;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import java.time.Instant;
import java.time.LocalDate;
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

    @Mock
    private IUserService userService;

    @Mock
    private IProfileService profileService;

    @Mock
    private IRoleService roleService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IPasswordEncoder passwordEncoder;

    @Mock
    private IJwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_doesNotTrimPassword() {
        RegisterCommand command = new RegisterCommand(
                "username",
                "  secret  ",
                "0123456789",
                "test@gmail.com",
                "first",
                "last",
                null,
                Gender.MALE,
                LocalDate.of(2000, 1, 1)
        );

        when(userService.create(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileService.create(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleService.getByName(RoleName.USER)).thenReturn(defaultRole());
        when(passwordEncoder.encode("  secret  ")).thenReturn("hashed-secret");

        RegisterResult result = authService.register(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).create(userCaptor.capture());
        verify(passwordEncoder).encode("  secret  ");

        assertEquals("hashed-secret", userCaptor.getValue().getPasswordHash());
        assertEquals("username", result.username());
    }

    @Test
    void login_doesNotTrimPassword() {
        String password = "  secret  ";
        User user = activeUserWithPassword(password);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("jwt-token");

        var result = authService.login(new LoginCommand("username", password));

        assertEquals(user.getId(), result.userId());
        assertEquals(UserStatus.ACTIVE, result.status());
        assertTrue(result.roles().contains(RoleName.USER));
        assertEquals("jwt-token", result.accessToken());
    }

    @Test
    void login_rejectsMissingUser() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        ApplicationException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ApplicationException.class,
                () -> authService.login(new LoginCommand("missing", "secret"))
        );

        assertEquals(ApplicationErrorCode.AUTH_USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void login_rejectsInactiveUser() {
        User user = userWithPassword(UserStatus.INACTIVE, false, null, "secret");
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        DomainException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DomainException.class,
                () -> authService.login(new LoginCommand("username", "secret"))
        );

        assertEquals(DomainErrorCode.USER_NOT_ACTIVE_CANNOT_LOGIN, exception.getErrorCode());
    }

    @Test
    void login_rejectsLockedUser() {
        User user = userWithPassword(UserStatus.ACTIVE, true, null, "secret");
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        DomainException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DomainException.class,
                () -> authService.login(new LoginCommand("username", "secret"))
        );

        assertEquals(DomainErrorCode.BLOCKED_USER_CANNOT_LOGIN, exception.getErrorCode());
    }

    @Test
    void login_rejectsDeletedUser() {
        User user = userWithPassword(UserStatus.ACTIVE, false, Instant.EPOCH, "secret");
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        DomainException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DomainException.class,
                () -> authService.login(new LoginCommand("username", "secret"))
        );

        assertEquals(DomainErrorCode.DELETED_USER_CANNOT_LOGIN, exception.getErrorCode());
    }

    @Test
    void login_rejectsInvalidPassword() {
        User user = userWithPassword(UserStatus.ACTIVE, false, null, "correct-password");
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
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
                RoleName.USER,
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
                        RoleName.USER,
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
