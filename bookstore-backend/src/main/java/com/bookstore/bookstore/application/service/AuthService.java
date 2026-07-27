package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.domain.enums.AuditAction;
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
import com.bookstore.bookstore.application.exception.AuthRateLimitException;
import com.bookstore.bookstore.application.exception.GoogleIdTokenVerificationException;
import com.bookstore.bookstore.application.port.in.IAuthService;
import com.bookstore.bookstore.application.port.in.IOtpService;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.port.in.IAuditLogService;
import com.bookstore.bookstore.application.port.out.IAuthThrottleService;
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
import com.bookstore.bookstore.application.result.SessionResult;
import com.bookstore.bookstore.domain.enums.AuditTargetType;
import com.bookstore.bookstore.domain.enums.AuthProvider;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.PasswordResetToken;
import com.bookstore.bookstore.domain.model.RefreshToken;
import com.bookstore.bookstore.domain.enums.RefreshTokenRevokeReason;
import com.bookstore.bookstore.application.command.AuthRequestMetadata;
import com.bookstore.bookstore.application.command.AuditLogCommand;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.domain.model.UserAuthIdentity;
import com.bookstore.bookstore.shared.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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
    private final IAuthThrottleService authThrottleService;
    private final IAuditLogService auditLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResult register(RegisterCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Instant now = Instant.now();
        String email = StringUtils.trimToNull(command.email());
        Role defaultRole = loadDefaultUserRole();
        String passwordHash = passwordEncoder.encode(command.password());

        User deletedUser = userRepository.findByEmailIncludingDeleted(email)
                .filter(user -> user.getDeletedAt() != null)
                .orElse(null);
        if (deletedUser != null) {
            deletedUser.restoreForRegistration(passwordHash, Set.of(defaultRole));
            User restoredUser = userRepository.save(deletedUser);
            profileService.restoreForUser(restoredUser.getId());
            refreshTokenRepository.revokeAllByUserId(
                    restoredUser.getId(), now, RefreshTokenRevokeReason.SESSION_REVOKED
            );
            otpService.sendRegistrationOtp(restoredUser);
            audit(restoredUser, AuditAction.ACCOUNT_RESTORED_FOR_REGISTRATION, AuditTargetType.USER, restoredUser.getId().toString(),
                    AuthRequestMetadata.empty(), null);
            return new RegisterResult(restoredUser.getUsername(), restoredUser.getCreatedAt());
        }

        User user = new User(
                UUID.randomUUID(),
                email,
                passwordHash,
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
            return issueTokens(user, AuthRequestMetadata.empty(), null, null);
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
            return issueTokens(user, AuthRequestMetadata.empty(), null, null);
        }

        userAuthIdentityRepository.save(createGoogleIdentity(user.getId(), googleIdToken));
        return issueTokens(user, AuthRequestMetadata.empty(), null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResult login(LoginCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String username = normalizeLoginIdentifier(command.username());
        String password = command.password();
        AuthRequestMetadata metadata = command.metadata();

        if (authThrottleService != null) {
            try {
                authThrottleService.assertLoginAllowed(username, metadata.ipAddress());
            } catch (AuthRateLimitException exception) {
                audit(null, AuditAction.LOGIN_THROTTLED, AuditTargetType.LOGIN, null, metadata, null);
                throw exception;
            } catch (RuntimeException exception) {
                log.warn("Login rate limiter unavailable during pre-check", exception);
            }
        }

        User user = userRepository.findByUsernameActive(username).orElse(null);
        if (user == null) {
            // Keep password verification timing comparable for an unknown account.
            passwordEncoder.matches(password, "$2a$10$7EqJtq98hPqEX7fNZaFWoO5L0w8F3e5l7WfZMQIFwHixh0yNfEDYa");
            recordLoginFailure(username, metadata);
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        if (!user.hasPassword() || !passwordEncoder.matches(password, user.getPasswordHash())) {
            recordLoginFailure(username, metadata);
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        try {
            user.requireCanLogin();
        } catch (DomainException exception) {
            recordLoginFailure(username, metadata);
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        if (authThrottleService != null) {
            try {
                authThrottleService.clearLoginFailures(username);
            } catch (RuntimeException exception) {
                log.warn("Login rate limiter unavailable while clearing failures", exception);
            }
        }
        audit(user, AuditAction.LOGIN_SUCCEEDED, AuditTargetType.USER, user.getId().toString(), metadata, null);
        return issueTokens(user, metadata, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResult refresh(RefreshAccessTokenCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String rawRefreshToken = StringUtils.trimToNull(command.refreshToken());
        String tokenHash = hashRefreshToken(rawRefreshToken);
        RefreshToken preview = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_INVALID_REFRESH_TOKEN));

        // Lock the user first so logout-all/password reset cannot miss a newly rotated session.
        User user = loadUserForRefreshWithLock(preview.getUserId());
        RefreshToken currentRefreshToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElse(preview);
        Instant now = Instant.now();

        if (currentRefreshToken.isRevoked()) {
            if (currentRefreshToken.getRevokeReason() == RefreshTokenRevokeReason.ROTATED) {
                refreshTokenRepository.revokeFamily(
                        currentRefreshToken.getFamilyId(), now, RefreshTokenRevokeReason.FAMILY_COMPROMISED
                );
                audit(user, AuditAction.REFRESH_TOKEN_REUSE_DETECTED, AuditTargetType.REFRESH_TOKEN_FAMILY,
                        currentRefreshToken.getFamilyId().toString(), command.metadata(), null);
                throw new ApplicationException(ApplicationErrorCode.AUTH_REFRESH_REUSE_DETECTED);
            }
            throw new ApplicationException(ApplicationErrorCode.AUTH_SESSION_REVOKED);
        }

        if (currentRefreshToken.isExpiredAt(now)) {
            currentRefreshToken.revoke(now, RefreshTokenRevokeReason.SESSION_REVOKED);
            refreshTokenRepository.save(currentRefreshToken);
            throw new ApplicationException(ApplicationErrorCode.AUTH_SESSION_EXPIRED);
        }

        String nextRawRefreshToken = generateSecureTokenValue();
        RefreshToken nextRefreshToken = createRefreshToken(
                user.getId(), nextRawRefreshToken, currentRefreshToken.getFamilyId(), currentRefreshToken.getId(), command.metadata()
        );
        currentRefreshToken.rotateTo(nextRefreshToken.getId(), now);
        currentRefreshToken.markUsed(now);
        refreshTokenRepository.save(currentRefreshToken);
        refreshTokenRepository.save(nextRefreshToken);
        audit(user, AuditAction.REFRESH_TOKEN_ROTATED, AuditTargetType.REFRESH_TOKEN, nextRefreshToken.getId().toString(), command.metadata(), null);
        return toLoginResult(user, nextRefreshToken, nextRawRefreshToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(LogoutCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String rawRefreshToken = StringUtils.trimToNull(command.refreshToken());
        String tokenHash = hashRefreshToken(rawRefreshToken);
        RefreshToken preview = refreshTokenRepository.findByTokenHash(tokenHash).orElse(null);
        if (preview == null) {
            return;
        }
        User user = userRepository.findByIdIncludingDeletedForUpdate(preview.getUserId())
                .or(() -> userRepository.findByIdIncludingDeleted(preview.getUserId()))
                .orElse(null);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash).orElse(preview);
        if (refreshToken != null && !refreshToken.isRevoked()) {
            refreshToken.revoke(Instant.now(), RefreshTokenRevokeReason.LOGOUT);
            refreshTokenRepository.save(refreshToken);
            audit(user, AuditAction.SESSION_REVOKED, AuditTargetType.REFRESH_TOKEN, refreshToken.getId().toString(), command.metadata(), null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestPasswordResetOtp(RequestPasswordResetOtpCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String email = StringUtils.trimToNull(command.email());
        AuthRequestMetadata metadata = command.metadata();
        if (authThrottleService != null) {
            authThrottleService.assertPasswordResetAllowed(email, metadata.ipAddress());
            authThrottleService.recordPasswordResetRequest(email, metadata.ipAddress());
        }

        userRepository.findByEmailIncludingDeleted(email).ifPresent(user -> {
            try {
                user.requireCanLogin();
                otpService.sendPasswordResetOtp(user);
            } catch (DomainException exception) {
                // Không throw ra FE để tránh lộ tài khoản bị khóa/ban/xóa
            }
        });
        audit(null, AuditAction.PASSWORD_RESET_REQUESTED, AuditTargetType.PASSWORD_RESET, null, metadata, java.util.Map.of("email", "redacted"));
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
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByTokenHashForUpdate(hashPasswordResetToken(rawResetToken))
                .or(() -> passwordResetTokenRepository.findByTokenHash(hashPasswordResetToken(rawResetToken)))
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_INVALID_PASSWORD_RESET_TOKEN));

        if (passwordResetToken.isUsed()) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_PASSWORD_RESET_TOKEN);
        }

        Instant now = Instant.now();
        if (passwordResetToken.isExpiredAt(now)) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_PASSWORD_RESET_TOKEN_EXPIRED);
        }

        User user = loadUserForPasswordResetWithLock(passwordResetToken.getUserId());
        passwordResetToken.markUsed(now);
        passwordResetTokenRepository.save(passwordResetToken);
        user.updatePasswordHash(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(user.getId(), now, RefreshTokenRevokeReason.PASSWORD_RESET);
        audit(user, AuditAction.PASSWORD_RESET_COMPLETED, AuditTargetType.USER, user.getId().toString(), AuthRequestMetadata.empty(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logoutAll(UUID userId) {
        User user = loadUserForRefreshWithLock(userId);
        Instant now = Instant.now();
        refreshTokenRepository.revokeAllByUserId(user.getId(), now, RefreshTokenRevokeReason.LOGOUT_ALL);
        audit(user, AuditAction.ALL_SESSIONS_REVOKED, AuditTargetType.USER, user.getId().toString(), AuthRequestMetadata.empty(), null);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<SessionResult> getSessions(UUID userId, UUID currentSessionId) {
        return refreshTokenRepository.findActiveByUserId(userId).stream()
                .map(token -> new SessionResult(
                        token.getId(),
                        token.getDeviceName(),
                        token.getDeviceId(),
                        token.getUserAgent(),
                        maskIp(token.getIpAddress()),
                        token.getCreatedAt(),
                        token.getLastUsedAt(),
                        token.getExpiresAt(),
                        token.getId().equals(currentSessionId)
                ))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeSession(UUID userId, UUID sessionId, UUID currentSessionId) {
        User user = loadUserForRefreshWithLock(userId);
        RefreshToken token = refreshTokenRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_SESSION_REVOKED));
        if (!token.getUserId().equals(user.getId())) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_SESSION_REVOKED);
        }
        if (token.getRevokeReason() == RefreshTokenRevokeReason.ROTATED) {
            refreshTokenRepository.revokeFamily(token.getFamilyId(), Instant.now(), RefreshTokenRevokeReason.SESSION_REVOKED);
            audit(user, AuditAction.SESSION_REVOKED, AuditTargetType.REFRESH_TOKEN_FAMILY, token.getFamilyId().toString(), AuthRequestMetadata.empty(), null);
            return;
        }
        if (!token.isRevoked()) {
            token.revoke(Instant.now(), RefreshTokenRevokeReason.SESSION_REVOKED);
            refreshTokenRepository.save(token);
            audit(user, AuditAction.SESSION_REVOKED, AuditTargetType.REFRESH_TOKEN, token.getId().toString(), AuthRequestMetadata.empty(),
                    java.util.Map.of("currentSession", token.getId().equals(currentSessionId)));
        }
    }

    private LoginResult issueTokens(User user, AuthRequestMetadata metadata, UUID familyId, UUID parentTokenId) {
        String rawRefreshToken = generateSecureTokenValue();
        RefreshToken refreshToken = createRefreshToken(user.getId(), rawRefreshToken, familyId, parentTokenId, metadata);
        refreshTokenRepository.save(refreshToken);
        return toLoginResult(user, refreshToken, rawRefreshToken);
    }

    private LoginResult toLoginResult(User user, RefreshToken refreshToken, String rawRefreshToken) {
        String accessToken = jwtService.generateAccessToken(user, refreshToken.getId());
        return new LoginResult(
                user.getId(),
                user.getStatus(),
                toRoleNames(user.getRoles()),
                accessToken,
                rawRefreshToken
        );
    }

    private User loadUserForRefreshWithLock(UUID userId) {
        User user = userRepository.findByIdIncludingDeletedForUpdate(userId)
                .or(() -> userRepository.findByIdIncludingDeleted(userId))
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_INVALID_REFRESH_TOKEN));
        try {
            user.requireCanLogin();
        } catch (DomainException exception) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }
        return user;
    }

    private User loadUserForPasswordResetWithLock(UUID userId) {
        User user = userRepository.findByIdIncludingDeletedForUpdate(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_INVALID_PASSWORD_RESET_TOKEN));
        try {
            user.requireCanLogin();
        } catch (DomainException exception) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_PASSWORD_RESET_TOKEN);
        }
        return user;
    }

    private RefreshToken createRefreshToken(
            UUID userId,
            String rawRefreshToken,
            UUID familyId,
            UUID parentTokenId,
            AuthRequestMetadata metadata
    ) {
        Instant now = Instant.now();
        UUID tokenId = UUID.randomUUID();
        return new RefreshToken(
                tokenId,
                userId,
                hashRefreshToken(rawRefreshToken),
                familyId == null ? tokenId : familyId,
                parentTokenId,
                null,
                metadata == null ? null : metadata.deviceId(),
                metadata == null ? null : metadata.deviceName(),
                metadata == null ? null : metadata.userAgent(),
                metadata == null ? null : metadata.ipAddress(),
                now,
                now,
                null,
                null,
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

    private String normalizeLoginIdentifier(String value) {
        String normalized = StringUtils.trimToNull(value);
        return normalized == null ? "" : normalized.toLowerCase(java.util.Locale.ROOT);
    }

    private String maskIp(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.contains(":")) {
            int index = value.lastIndexOf(':');
            return index <= 0 ? "***" : value.substring(0, index) + ":***";
        }
        int index = value.lastIndexOf('.');
        return index <= 0 ? "***" : value.substring(0, index) + ".***";
    }

    private void recordLoginFailure(String normalizedIdentifier, AuthRequestMetadata metadata) {
        if (authThrottleService != null) {
            try {
                authThrottleService.recordLoginFailure(
                        normalizedIdentifier,
                        metadata == null ? null : metadata.ipAddress()
                );
            } catch (RuntimeException exception) {
                log.warn("Login rate limiter unavailable while recording a failed login", exception);
            }
        }
        audit(null, AuditAction.LOGIN_FAILED, AuditTargetType.LOGIN, null, metadata, java.util.Map.of("identifier", "redacted"));
    }

    private void audit(
            User user,
            AuditAction action,
            AuditTargetType targetType,
            String targetId,
            AuthRequestMetadata metadata,
            Object details
    ) {
        if (auditLogService == null) {
            return;
        }
        try {
            auditLogService.record(new AuditLogCommand(
                    user == null ? null : user.getId(),
                    user == null ? null : user.getUsername(),
                    user == null ? null : toRoleNames(user.getRoles()).stream().findFirst().orElse(null),
                    action,
                    targetType,
                    targetId,
                    action.name(),
                    null,
                    details,
                    metadata == null ? null : metadata.ipAddress(),
                    metadata == null ? null : metadata.userAgent(),
                    Instant.now()
            ));
        } catch (RuntimeException exception) {
            log.warn("Authentication audit log unavailable for action {}", action, exception);
        }
    }

    private static Set<String> toRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
