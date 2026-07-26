package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateUserCommand;
import com.bookstore.bookstore.application.command.DeleteUserCommand;
import com.bookstore.bookstore.application.command.UpdateStaffUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserLockCommand;
import com.bookstore.bookstore.application.command.UpdateUserCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import com.bookstore.bookstore.application.port.out.IProfileRepository;
import com.bookstore.bookstore.application.port.out.IRoleRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.shared.util.StringUtils;

import java.time.Instant;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String STAFF_ROLE = "STAFF";
    private static final String SHIPPER_ROLE = "SHIPPER";
    private static final String USER_ROLE = "USER";

    private final IUserRepository userRepository;
    private final IProfileRepository profileRepository;
    private final IProfileService profileService;
    private final IRoleRepository roleRepository;
    private final IPasswordEncoder passwordEncoder;
    private final FileAssetPolicyService fileAssetPolicyService;

    @Override
    public List<User> getAll() {
        return userRepository.findAllActive();
    }

    @Override
    public List<User> getCustomers() {
        return getActiveUsersByRole(USER_ROLE);
    }

    @Override
    public PageSliceResult<User> getCustomers(int page, int size) {
        return getActiveUsersByRole(USER_ROLE, page, size);
    }

    @Override
    public List<User> getStaffs() {
        return getActiveUsersByRole(STAFF_ROLE);
    }

    @Override
    public PageSliceResult<User> getStaffs(int page, int size) {
        return getActiveUsersByRole(STAFF_ROLE, page, size);
    }

    @Override
    public List<User> getAdmins() {
        return getActiveUsersByRole(ADMIN_ROLE);
    }

    @Override
    public PageSliceResult<User> getAdmins(int page, int size) {
        return getActiveUsersByRole(ADMIN_ROLE, page, size);
    }

    @Override
    public List<User> getShippers() {
        return getActiveUsersByRole(SHIPPER_ROLE);
    }

    @Override
    public PageSliceResult<User> getShippers(int page, int size) {
        return getActiveUsersByRole(SHIPPER_ROLE, page, size);
    }

    @Override
    public List<User> getAllIncludingDeleted() {
        return userRepository.findAllIncludingDeleted();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public User create(User user) {
        if (user == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "user");
        }

        if (userRepository.existsByUsernameIncludingDeleted(user.getUsername())) {
            throw new ApplicationException(ApplicationErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }

        String phoneNumber = user.getPhoneNumber();
        if (phoneNumber != null) {
            if( userRepository.existsByPhoneNumberIncludingDeleted(phoneNumber)) {
                throw new ApplicationException(ApplicationErrorCode.USER_PHONE_ALREADY_EXISTS);
            }
        }
             

        if (userRepository.existsByEmailIncludingDeleted(user.getEmail())) {
            throw new ApplicationException(ApplicationErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createByAdmin(CreateUserCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Instant now = Instant.now();
        String roleName = normalizeManagedRole(command.roleName());
        Role role = roleRepository.findByNameActive(roleName)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ROLE_NOT_FOUND));

        User user = new User(
                UUID.randomUUID(),
                command.username(),
                passwordEncoder.encode(command.password()),
                command.phoneNumber(),
                command.email(),
                UserStatus.ACTIVE,
                false,
                Set.of(role),
                now,
                now,
                null
        );

        User savedUser = create(user);
        FileAsset avatarFileAsset = resolveAvatarFileAsset(command.avatarFileAssetId());
        profileService.create(new Profile(
                UUID.randomUUID(),
                savedUser.getId(),
                command.lastName(),
                command.firstName(),
                avatarFileAsset,
                command.gender(),
                command.dateOfBirth(),
                now,
                now,
                null
        ));

        return savedUser;
    }

    @Override
    public User getById(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        return userRepository.findByIdActive(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User getByIdIncludingDeleted(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        return userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User update(UpdateUserCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "user");
        }

        UUID userId = command.userId();
        User currentUser = userRepository.findByIdActive(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));

        String username = StringUtils.trimToNull(command.username());
        String phoneNumber = StringUtils.trimToNull(command.phoneNumber());
        String email = StringUtils.trimToNull(command.email());

        if (!currentUser.getUsername().equals(username)
                && userRepository.existsByUsernameIncludingDeleted(username)) {
            throw new ApplicationException(ApplicationErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }

        if (!Objects.equals(currentUser.getPhoneNumber(), phoneNumber)
                && phoneNumber != null
                && userRepository.existsByPhoneNumberIncludingDeleted(phoneNumber)) {
            throw new ApplicationException(ApplicationErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        if (!currentUser.getEmail().equals(email)
                && userRepository.existsByEmailIncludingDeleted(email)) {
            throw new ApplicationException(ApplicationErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        currentUser.updateAccountInfo(username, email, phoneNumber);
        return userRepository.save(currentUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateStaffByAdmin(UpdateStaffUserCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UUID userId = command.userId();
        User currentUser = userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.STAFF_NOT_FOUND));

        if (!currentUser.hasRole(STAFF_ROLE)
                && !currentUser.hasRole(SHIPPER_ROLE)
                && !currentUser.hasRole(ADMIN_ROLE)) {
            throw new ApplicationException(ApplicationErrorCode.STAFF_NOT_FOUND);
        }

        String phoneNumber = StringUtils.trimToNull(command.phoneNumber());
        String email = StringUtils.trimToNull(command.email());
        Set<Role> roles = resolveManagedRoles(command.roleNames());

        if (!Objects.equals(currentUser.getPhoneNumber(), phoneNumber)
                && phoneNumber != null
                && userRepository.existsByPhoneNumberIncludingDeleted(phoneNumber)) {
            throw new ApplicationException(ApplicationErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        if (!currentUser.getEmail().equals(email)
                && userRepository.existsByEmailIncludingDeleted(email)) {
            throw new ApplicationException(ApplicationErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        currentUser.updateManagedInfo(email, phoneNumber, roles);
        return userRepository.save(currentUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateLockByAdmin(UpdateUserLockCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        if (command.userId().equals(command.adminId())) {
            throw new ApplicationException(ApplicationErrorCode.USER_SELF_MANAGEMENT_NOT_ALLOWED);
        }

        User currentUser = userRepository.findByIdIncludingDeleted(command.userId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));

        currentUser.updateLockStatus(command.locked());
        return userRepository.save(currentUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAdmin(DeleteUserCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        if (command.userId().equals(command.adminId())) {
            throw new ApplicationException(ApplicationErrorCode.USER_SELF_MANAGEMENT_NOT_ALLOWED);
        }

        delete(command);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteUserCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UUID userId = command.userId();
        UUID adminId = command.adminId();

        // Admin xoa user khac, con user co the tu xoa chinh minh.
        if (!adminId.equals(userId)) {
            User admin = userRepository.findByIdActive(adminId)
                    .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));

            if (!admin.hasRole(ADMIN_ROLE)){
                throw new ApplicationException(ApplicationErrorCode.USER_NOT_ADMIN);
            }
        }

        User currentUser = userRepository.findByIdIncludingDeleted(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));

        currentUser.softDelete();
        userRepository.save(currentUser);

        // xoa profile
        profileRepository.findByUserIdActive(userId)
                .ifPresent(profile -> profileService.delete(profile.getId()));
    }

    private List<User> getActiveUsersByRole(String roleName) {
        return userRepository.findAllActive().stream()
                .filter(user -> user.hasRole(roleName))
                .toList();
    }

    private PageSliceResult<User> getActiveUsersByRole(String roleName, int page, int size) {
        validatePageRequest(page, size);
        return userRepository.findPageByRoleNameActive(roleName, page, size);
    }

    private String normalizeManagedRole(String roleName) {
        String normalizedRoleName = StringUtils.trimToNull(roleName);
        if (normalizedRoleName == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "roleName");
        }

        String upperRoleName = normalizedRoleName.toUpperCase(Locale.ROOT);
        if (!STAFF_ROLE.equals(upperRoleName)
                && !ADMIN_ROLE.equals(upperRoleName)
                && !SHIPPER_ROLE.equals(upperRoleName)) {
            throw new ApplicationException(ApplicationErrorCode.USER_ROLE_NOT_ALLOWED);
        }

        return upperRoleName;
    }

    private Set<Role> resolveManagedRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "roleNames");
        }

        Set<Role> roles = new LinkedHashSet<>();
        for (String roleName : roleNames) {
            String normalizedRoleName = normalizeManagedRole(roleName);
            Role role = roleRepository.findByNameActive(normalizedRoleName)
                    .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ROLE_NOT_FOUND));
            roles.add(role);
        }

        return roles;
    }

    private FileAsset resolveAvatarFileAsset(UUID avatarFileAssetId) {
        if (avatarFileAssetId == null) {
            return null;
        }

        return fileAssetPolicyService.requireActiveAsset(
                avatarFileAssetId,
                FilePurpose.USER_AVATAR,
                FileVisibility.PUBLIC
        );
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page");
        }

        if (size <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "size");
        }
    }
}
