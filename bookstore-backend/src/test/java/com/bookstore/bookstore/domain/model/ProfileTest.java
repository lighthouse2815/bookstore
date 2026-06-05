package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.Gender;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProfileTest {

    @Test
    void updateProfileInfo_rejectsDeletedProfile() {
        Profile profile = deletedProfile();

        DomainException exception = assertThrows(DomainException.class, () ->
                profile.updateProfileInfo(
                        "new-last",
                        "new-first",
                        "new-avatar",
                        Gender.FEMALE,
                        LocalDate.of(2000, 1, 1)
                )
        );

        assertEquals(DomainErrorCode.DELETED_PROFILE_CANNOT_UPDATE_PROFILE_INFO, exception.getErrorCode());
    }

    @Test
    void softDelete_rejectsDeletedProfile() {
        Profile profile = deletedProfile();

        DomainException exception = assertThrows(DomainException.class, profile::softDelete);

        assertEquals(DomainErrorCode.PROFILE_ALREADY_DELETED, exception.getErrorCode());
    }

    private static Profile deletedProfile() {
        Instant deletedAt = Instant.EPOCH;
        return new Profile(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "last",
                "first",
                "avatar",
                Gender.MALE,
                LocalDate.of(2000, 1, 1),
                deletedAt,
                deletedAt,
                deletedAt
        );
    }
}
