package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.UpdateProfileCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IProfileService;
import com.bookstore.bookstore.application.port.out.IProfileRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.model.Profile;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService implements IProfileService {

    private final IProfileRepository profileRepository;
    private final IUserRepository userRepository;

    public ProfileService(IProfileRepository profileRepository, IUserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Profile> getAll() {
        return profileRepository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Profile create(Profile profile) {
        if (profile == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "profile");
        }

        if (!userRepository.existsById(profile.getUserId())) {
            throw new ApplicationException(ApplicationErrorCode.PROFILE_USER_NOT_FOUND);
        }

        if (profileRepository.existsByUserId(profile.getUserId())) {
            throw new ApplicationException(ApplicationErrorCode.PROFILE_USER_ALREADY_HAS_PROFILE);
        }

        return profileRepository.save(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Profile update(UpdateProfileCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UUID userId = command.userId();
        if (!userRepository.existsById(userId)) {
            throw new ApplicationException(ApplicationErrorCode.PROFILE_USER_NOT_FOUND);
        }

        Profile currentProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PROFILE_NOT_FOUND));

        currentProfile.updateProfileInfo(
                command.lastName(),
                command.firstName(),
                command.avatarUrl(),
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

        Profile currentProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PROFILE_NOT_FOUND));

        currentProfile.softDelete();
        profileRepository.save(currentProfile);
    }
}
