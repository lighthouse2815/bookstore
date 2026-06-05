package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.UpdateProfileCommand;
import com.bookstore.bookstore.domain.model.Profile;
import java.util.List;
import java.util.UUID;

public interface IProfileService {

    List<Profile> getAll();

    Profile create(Profile profile);

    Profile update(UpdateProfileCommand command);

    void delete(UUID profileId);
}
