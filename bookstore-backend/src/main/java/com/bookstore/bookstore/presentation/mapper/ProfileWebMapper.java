package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.UpdateProfileCommand;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.presentation.request.UpdateProfileRequest;
import com.bookstore.bookstore.presentation.response.ProfileResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProfileWebMapper {

    public UpdateProfileCommand toUpdateCommand(UUID userId, UpdateProfileRequest request) {
        return new UpdateProfileCommand(
                userId,
                request.lastName(),
                request.firstName(),
                request.avatarUrl(),
                request.gender(),
                request.dateOfBirth()
        );
    }

    public ProfileResponse toProfileResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getLastName(),
                profile.getFirstName(),
                profile.getAvatarUrl(),
                profile.getGender(),
                profile.getDateOfBirth(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
