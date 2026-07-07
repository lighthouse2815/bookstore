package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.GoogleLoginCommand;
import com.bookstore.bookstore.application.command.LoginCommand;
import com.bookstore.bookstore.application.command.LogoutCommand;
import com.bookstore.bookstore.application.command.RefreshAccessTokenCommand;
import com.bookstore.bookstore.application.command.RegisterCommand;
import com.bookstore.bookstore.application.command.RequestPasswordResetOtpCommand;
import com.bookstore.bookstore.application.command.ResetPasswordCommand;
import com.bookstore.bookstore.application.command.VerifyOtpCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.exception.GoogleIdTokenVerificationException;
import com.bookstore.bookstore.application.port.in.IAuthService;
import com.bookstore.bookstore.application.port.in.IOtpService;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.port.out.IGoogleIdTokenVerifier;
import com.bookstore.bookstore.application.port.out.IJwtService;
import com.bookstore.bookstore.application.port.out.IPasswordResetTokenRepository;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IRefreshTokenRepository;
import com.bookstore.bookstore.application.port.out.IRoleRepository;
import com.bookstore.bookstore.application.port.out.IUserAuthIdentityRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.port.out.VerifiedGoogleIdToken;
import com.bookstore.bookstore.application.result.LoginResult;
import com.bookstore.bookstore.application.result.PasswordResetTokenResult;
import com.bookstore.bookstore.application.result.RegisterResult;
import com.bookstore.bookstore.domain.enums.AuthProvider;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.PasswordResetToken;
import com.bookstore.bookstore.domain.model.RefreshToken;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.domain.model.UserAuthIdentity;
import com.bookstore.bookstore.shared.util.StringUtils;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private static final String USER_ROLE = "USER";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES = 10;

    private final IUserService userService;
    private final IProfileService profileService;
    private final IOtpService otpService;
    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;
    private final IUserAuthIdentityRepository userAuthIdentityRepository;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final IPasswordEncoder passwordEncoder;
    private final IJwtService jwtService;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IGoogleIdTokenVerifier googleIdTokenVerifier;

    // TODO : THEM CHUC NANG TAO MOI TAI KHOAN KHI DA CO TAI KHOAN KHOA, CHECK TRONG BANG DELETE_USER
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResult register(RegisterCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Instant now = Instant.now();
        String email = StringUtils.trimToNull(command.email());
        Role defaultRole = loadDefaultUserRole();
        User user = new User(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(command.password()),
                null,
                email,
                UserStatus.INACTIVE,
                false,
                Set.of(defaultRole),
                now,
                now,
                null
        );

        User savedUser = userService.create(user);

        Profile profile = new Profile(
                    UUID.randomUUID(),
                    savedUser.getId(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    now,
                    now,
                    null
        );

        profileService.create(profile);
        otpService.sendRegistrationOtp(savedUser);

        return new RegisterResult(savedUser.getUsername(), savedUser.getCreatedAt());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResult loginWithGoogle(GoogleLoginCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        VerifiedGoogleIdToken googleIdToken = verifyGoogleIdToken(command.idToken());
        if (!googleIdToken.emailVerified()) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_GOOGLE_EMAIL_NOT_VERIFIED);
        }

        UserAuthIdentity identity = userAuthIdentityRepository.findByProviderAndProviderSubject(
                        AuthProvider.GOOGLE,
                        googleIdToken.subject()
                )
                .orElse(null);

        if (identity != null) {
            identity.syncProviderState(googleIdToken.email(), googleIdToken.emailVerified());
            userAuthIdentityRepository.save(identity);
            User user = prepareUserForGoogleLogin(loadUserForGoogleIdentity(identity.getUserId()));
            return issueTokens(user);
        }

        User user = userRepository.findByEmailIncludingDeleted(googleIdToken.email())
                .map(this::prepareUserForGoogleLogin)
                .orElseGet(() -> createGoogleUser(googleIdToken));

        UserAuthIdentity existingIdentityForUser = userAuthIdentityRepository.findByUserIdAndProvider(
                        user.getId(),
                        AuthProvider.GOOGLE
                )
                .orElse(null);

        if (existingIdentityForUser != null) {
            if (!existingIdentityForUser.getProviderSubject().equals(googleIdToken.subject())) {
                throw new ApplicationException(ApplicationErrorCode.AUTH_GOOGLE_ACCOUNT_ALREADY_LINKED);
            }

            existingIdentityForUser.syncProviderState(googleIdToken.email(), googleIdToken.emailVerified());
            userAuthIdentityRepository.save(existingIdentityForUser);
            return issueTokens(user);
        }

        userAuthIdentityRepository.save(createGoogleIdentity(user.getId(), googleIdToken));
        return issueTokens(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResult login(LoginCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String username = StringUtils.trimToNull(command.username());
        String password = command.password();

        User user = userRepository.findByUsernameActive(username)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_USER_NOT_FOUND));

        user.requireCanLogin();

        if (!user.hasPassword()) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_PASSWORD_LOGIN_NOT_AVAILABLE);
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_PASSWORD);
        }

        return issueTokens(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResult refresh(RefreshAccessTokenCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String rawRefreshToken = StringUtils.trimToNull(command.refreshToken());
        RefreshToken currentRefreshToken = refreshTokenRepository.findByTokenHash(hashRefreshToken(rawRefreshToken))
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_INVALID_REFRESH_TOKEN));

        if (currentRefreshToken.isRevoked()) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }

        if (currentRefreshToken.isExpiredAt(Instant.now())) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        }

        User user = loadUserForRefresh(currentRefreshToken.getUserId());
        currentRefreshToken.revoke();
        refreshTokenRepository.save(currentRefreshToken);
        return issueTokens(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(LogoutCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String rawRefreshToken = StringUtils.trimToNull(command.refreshToken());
        refreshTokenRepository.findByTokenHash(hashRefreshToken(rawRefreshToken)).ifPresent(refreshToken -> {
            if (!refreshToken.isRevoked()) {
                refreshToken.revoke();
                refreshTokenRepository.save(refreshToken);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestPasswordResetOtp(RequestPasswordResetOtpCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String email = StringUtils.trimToNull(command.email());

        userRepository.findByEmailIncludingDeleted(email).ifPresent(user -> {
            try {
                user.requireCanLogin();
                otpService.sendPasswordResetOtp(user);
            } catch (DomainException exception) {
                // Không throw ra FE để tránh lộ tài khoản bị khóa/ban/xóa
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PasswordResetTokenResult verifyPasswordResetOtp(VerifyOtpCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        User user = otpService.verifyPasswordResetOtp(command);
        return createPasswordResetTokenResult(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String rawResetToken = StringUtils.trimToNull(command.resetToken());
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByTokenHash(hashPasswordResetToken(rawResetToken))
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_INVALID_PASSWORD_RESET_TOKEN));

        if (passwordResetToken.isUsed()) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_PASSWORD_RESET_TOKEN);
        }

        Instant now = Instant.now();
        if (passwordResetToken.isExpiredAt(now)) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_PASSWORD_RESET_TOKEN_EXPIRED);
        }

        User user = loadUserForPasswordReset(passwordResetToken.getUserId());
        passwordResetToken.markUsed(now);
        passwordResetTokenRepository.save(passwordResetToken);
        user.updatePasswordHash(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    private LoginResult issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = generateSecureTokenValue();
        RefreshToken refreshToken = createRefreshToken(user.getId(), rawRefreshToken);
        refreshTokenRepository.save(refreshToken);
        return new LoginResult(
                user.getId(),
                user.getStatus(),
                toRoleNames(user.getRoles()),
                accessToken,
                rawRefreshToken
        );
    }

    private User loadUserForRefresh(UUID userId) {
        User user = userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_INVALID_REFRESH_TOKEN));
        try {
            user.requireCanLogin();
        } catch (DomainException exception) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }
        return user;
    }

    private User loadUserForPasswordReset(UUID userId) {
        User user = userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_INVALID_PASSWORD_RESET_TOKEN));
        try {
            user.requireCanLogin();
        } catch (DomainException exception) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_PASSWORD_RESET_TOKEN);
        }
        return user;
    }

    private RefreshToken createRefreshToken(UUID userId, String rawRefreshToken) {
        Instant now = Instant.now();
        return new RefreshToken(
                UUID.randomUUID(),
                userId,
                hashRefreshToken(rawRefreshToken),
                jwtService.calculateRefreshTokenExpiresAt(now),
                false,
                now
        );
    }

    private PasswordResetTokenResult createPasswordResetTokenResult(UUID userId) {
        Instant now = Instant.now();
        String rawToken = generateSecureTokenValue();
        passwordResetTokenRepository.markUnusedByUserIdAsUsed(userId, now);
        PasswordResetToken passwordResetToken = new PasswordResetToken(
                UUID.randomUUID(),
                userId,
                hashPasswordResetToken(rawToken),
                now.plusSeconds(PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES * 60),
                null,
                now
        );
        passwordResetTokenRepository.save(passwordResetToken);
        return new PasswordResetTokenResult(rawToken, passwordResetToken.getExpiresAt());
    }

    private Role loadDefaultUserRole() {
        return roleRepository.findByNameActive(USER_ROLE)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ROLE_NOT_FOUND));
    }

    private VerifiedGoogleIdToken verifyGoogleIdToken(String idToken) {
        try {
            return googleIdTokenVerifier.verify(idToken);
        } catch (GoogleIdTokenVerificationException exception) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_GOOGLE_INVALID_ID_TOKEN);
        }
    }

    private User loadUserForGoogleIdentity(UUID userId) {
        return userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new IllegalStateException("User not found for Google identity: " + userId));
    }

    private User prepareUserForGoogleLogin(User user) {
        if (user.getStatus() == UserStatus.INACTIVE) {
            user.activate();
            return userRepository.save(user);
        }

        user.requireCanLogin();
        return user;
    }

    private User createGoogleUser(VerifiedGoogleIdToken googleIdToken) {
        Instant now = Instant.now();
        User user = new User(
                UUID.randomUUID(),
                resolveGoogleUsername(googleIdToken.email(), googleIdToken.subject()),
                null,
                null,
                googleIdToken.email(),
                UserStatus.ACTIVE,
                false,
                Set.of(loadDefaultUserRole()),
                now,
                now,
                null
        );

        User savedUser = userService.create(user);
        profileService.create(new Profile(
                UUID.randomUUID(),
                savedUser.getId(),
                googleIdToken.familyName(),
                googleIdToken.givenName(),
                null,
                null,
                null,
                now,
                now,
                null
        ));
        return savedUser;
    }

    private UserAuthIdentity createGoogleIdentity(UUID userId, VerifiedGoogleIdToken googleIdToken) {
        Instant now = Instant.now();
        return new UserAuthIdentity(
                UUID.randomUUID(),
                userId,
                AuthProvider.GOOGLE,
                googleIdToken.subject(),
                googleIdToken.email(),
                googleIdToken.emailVerified(),
                now,
                now
        );
    }

    private String resolveGoogleUsername(String email, String subject) {
        if (!userRepository.existsByUsernameIncludingDeleted(email)) {
            return email;
        }

        int atIndex = email.indexOf('@');
        String base = atIndex > 0 ? email.substring(0, atIndex) : email;
        String subjectSuffix = subject.length() > 8 ? subject.substring(subject.length() - 8) : subject;
        String candidate = truncateUsername(base + "_google_" + subjectSuffix);
        if (!userRepository.existsByUsernameIncludingDeleted(candidate)) {
            return candidate;
        }

        return truncateUsername("google_" + UUID.randomUUID().toString().replace("-", ""));
    }

    private String truncateUsername(String username) {
        return username.length() <= 100 ? username : username.substring(0, 100);
    }

    private String generateSecureTokenValue() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashRefreshToken(String rawToken) {
        return hashToken(rawToken, ApplicationErrorCode.AUTH_INVALID_REFRESH_TOKEN, "refresh token");
    }

    private String hashPasswordResetToken(String rawToken) {
        return hashToken(rawToken, ApplicationErrorCode.AUTH_INVALID_PASSWORD_RESET_TOKEN, "password reset token");
    }

    private String hashToken(String rawToken, ApplicationErrorCode errorCode, String tokenKind) {
        if (rawToken == null) {
            throw new ApplicationException(errorCode);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash " + tokenKind, exception);
        }
    }

    private static Set<String> toRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
