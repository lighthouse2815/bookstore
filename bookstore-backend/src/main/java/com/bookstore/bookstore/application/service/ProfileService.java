package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.UpdateProfileCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.out.IProfileRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.shared.util.StringUtils;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService implements IProfileService {

    private final IProfileRepository profileRepository;
    private final IUserRepository userRepository;

    @Override
    public List<Profile> getAll() {
        return profileRepository.findAllActive();
    }

    @Override
    public List<Profile> getAllIncludingDeleted() {
        return profileRepository.findAllIncludingDeleted();
    }

    // TODO : chuc nang tao acc lai khi da xoa + khoi phuc
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Profile create(Profile profile) {
        if (profile == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "profile");
        }

        if (!userRepository.existsByIdIncludingDeleted(profile.getUserId())) {
            throw new ApplicationException(ApplicationErrorCode.PROFILE_USER_NOT_FOUND);
        }

        if (profileRepository.existsByUserIdIncludingDeleted(profile.getUserId())) {
            throw new ApplicationException(ApplicationErrorCode.PROFILE_USER_ALREADY_HAS_PROFILE);
        }

        return profileRepository.save(profile);
    }

    @Override
    public Profile getByUserId(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        return profileRepository.findByUserIdActive(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PROFILE_NOT_FOUND));
    }

    @Override
    public Profile getByIdIncludingDeleted(UUID profileId) {
        if (profileId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "profileId");
        }

        return profileRepository.findByIdIncludingDeleted(profileId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PROFILE_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Profile update(UpdateProfileCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UUID userId = command.userId();
        if (userRepository.findByIdActive(userId).isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.PROFILE_USER_NOT_FOUND);
        }

        Profile currentProfile = profileRepository.findByUserIdActive(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PROFILE_NOT_FOUND));

        String lastName = StringUtils.trimToNull(command.lastName());
        String firstName = StringUtils.trimToNull(command.firstName());
        String avatarUrl = StringUtils.trimToNull(command.avatarUrl());

        currentProfile.updateProfileInfo(
                lastName,
                firstName,
                avatarUrl,
                command.gender(),
                command.dateOfBirth()
        );
        return profileRepository.save(currentProfile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID profileId) {
        if (profileId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "profileId");
        }

        Profile currentProfile = profileRepository.findByIdActive(profileId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PROFILE_NOT_FOUND));

        currentProfile.softDelete();
        profileRepository.save(currentProfile);
    }
}
