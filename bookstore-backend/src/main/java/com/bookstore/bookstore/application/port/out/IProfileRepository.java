package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Profile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IProfileRepository {

    List<Profile> findAll();

    Optional<Profile> findById(UUID profileId);

    Optional<Profile> findByUserId(UUID userId);

    boolean existsById(UUID profileId);

    boolean existsByUserId(UUID userId);

    void deleteById(UUID profileId);

    Profile save(Profile profile);
}
