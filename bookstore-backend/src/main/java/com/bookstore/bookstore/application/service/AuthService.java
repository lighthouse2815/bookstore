package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.LoginCommand;
import com.bookstore.bookstore.application.command.RegisterCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IAuthService;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.in.IRoleService;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.port.out.IJwtService;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.LoginResult;
import com.bookstore.bookstore.application.result.RegisterResult;
import com.bookstore.bookstore.domain.enums.RoleName;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService implements IAuthService {

    private final IUserService userService;
    private final IProfileService profileService;
    private final IRoleService roleService;
    private final IUserRepository userRepository;
    private final IPasswordEncoder passwordEncoder;
    private final IJwtService jwtService;

    public AuthService(
            IUserService userService,
            IProfileService profileService,
            IRoleService roleService,
            IUserRepository userRepository,
            IPasswordEncoder passwordEncoder,
            IJwtService jwtService
    ) {
        this.userService = userService;
        this.profileService = profileService;
        this.roleService = roleService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResult register(RegisterCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Instant now = Instant.now();
        Role defaultRole = roleService.getByName(RoleName.USER);
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
    public LoginResult login(LoginCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String username = StringUtils.trimToNull(command.username());
        String password = command.password();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTH_USER_NOT_FOUND));

        user.requireCanLogin();

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApplicationException(ApplicationErrorCode.AUTH_INVALID_PASSWORD);
        }

        String accessToken = jwtService.generateAccessToken(user);

        return new LoginResult(
                user.getId(),
                user.getStatus(),
                toRoleNames(user.getRoles()),
                accessToken
        );
    }

    private static Set<RoleName> toRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
