package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.LoginCommand;
import com.bookstore.bookstore.application.command.LogoutCommand;
import com.bookstore.bookstore.application.command.RefreshAccessTokenCommand;
import com.bookstore.bookstore.application.command.RegisterCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IAuthService;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.port.out.IJwtService;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IRefreshTokenRepository;
import com.bookstore.bookstore.application.port.out.IRoleRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.LoginResult;
import com.bookstore.bookstore.application.result.RegisterResult;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.RefreshToken;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.shared.util.StringUtils;

import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
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

    private final IUserService userService;
    private final IProfileService profileService;
    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;
    private final IPasswordEncoder passwordEncoder;
    private final IJwtService jwtService;
    private final IRefreshTokenRepository refreshTokenRepository;

    // TODO : THEM CHUC NANG TAO MOI TAI KHOAN KHI DA CO TAI KHOAN KHOA, CHECK TRONG BANG DELETE_USER
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResult register(RegisterCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Instant now = Instant.now();
        Role defaultRole = roleRepository.findByNameActive(USER_ROLE)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ROLE_NOT_FOUND));
        User user = new User(
                UUID.randomUUID(),
                command.username(),
                passwordEncoder.encode(command.password()),
                command.phoneNumber(),
                command.email(),
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
                    command.lastName(),
                    command.firstName(),
                    command.avatarUrl(),
                    command.gender(),
                    command.dateOfBirth(),
                    now,
                    now,
                    null
        );

        profileService.create(profile);

        return new RegisterResult(savedUser.getUsername(), savedUser.getCreatedAt());
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
        RefreshToken currentRefreshToken = refreshTokenRepository.findByToken(rawRefreshToken)
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
        refreshTokenRepository.findByToken(rawRefreshToken).ifPresent(refreshToken -> {
            if (!refreshToken.isRevoked()) {
                refreshToken.revoke();
                refreshTokenRepository.save(refreshToken);
            }
        });
    }

    private LoginResult issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user.getId());
        refreshTokenRepository.save(refreshToken);
        return new LoginResult(
                user.getId(),
                user.getStatus(),
                toRoleNames(user.getRoles()),
                accessToken,
                refreshToken.getToken()
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

    private RefreshToken createRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return new RefreshToken(
                UUID.randomUUID(),
                userId,
                generateRefreshTokenValue(),
                jwtService.calculateRefreshTokenExpiresAt(now),
                false,
                now
        );
    }

    private String generateRefreshTokenValue() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static Set<String> toRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
