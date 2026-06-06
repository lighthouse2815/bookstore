package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.DeleteUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.in.IUserService;
import com.bookstore.bookstore.application.port.out.IProfileRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.shared.util.StringUtils;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final IUserRepository userRepository;
    private final IProfileRepository profileRepository;
    private final IProfileService profileService;

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User create(User user) {
        if (user == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "user");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ApplicationException(ApplicationErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new ApplicationException(ApplicationErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ApplicationException(ApplicationErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User update(UpdateUserCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "user");
        }

        UUID userId = command.userId();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));

        String username = StringUtils.trimToNull(command.username());
        String phoneNumber = StringUtils.trimToNull(command.phoneNumber());
        String email = StringUtils.trimToNull(command.email());

        if (userRepository.existsByUsername(username)) {
            throw new ApplicationException(ApplicationErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ApplicationException(ApplicationErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(ApplicationErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        currentUser.updateAccountInfo(username, email, phoneNumber);
        return userRepository.save(currentUser);
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
            User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));

            if (!admin.hasRole(ADMIN_ROLE)){
                throw new ApplicationException(ApplicationErrorCode.USER_NOT_ADMIN);
            }
        }

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND));

        currentUser.softDelete();
        userRepository.save(currentUser);

        // xoa profile
        profileRepository.findByUserId(userId)
                .ifPresent(profile -> profileService.delete(profile.getId()));
    }
}
