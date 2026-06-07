package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Profile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IProfileRepository {

    List<Profile> findAllActive();

    List<Profile> findAllIncludingDeleted();

    Optional<Profile> findByIdActive(UUID profileId);

    Optional<Profile> findByIdIncludingDeleted(UUID profileId);

    Optional<Profile> findByUserIdActive(UUID userId);

    Optional<Profile> findByUserIdIncludingDeleted(UUID userId);

    boolean existsByIdIncludingDeleted(UUID profileId);

    boolean existsByUserIdIncludingDeleted(UUID userId);

    void deleteById(UUID profileId);

    Profile save(Profile profile);
}
