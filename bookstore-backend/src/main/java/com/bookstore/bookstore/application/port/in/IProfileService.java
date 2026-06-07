package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.UpdateProfileCommand;
import com.bookstore.bookstore.domain.model.Profile;
import java.util.List;
import java.util.UUID;

public interface IProfileService {

    List<Profile> getAll();

    List<Profile> getAllIncludingDeleted();

    Profile create(Profile profile);

    Profile getByUserId(UUID userId);

    Profile getByIdIncludingDeleted(UUID profileId);

    Profile update(UpdateProfileCommand command);

    void delete(UUID profileId);
}
