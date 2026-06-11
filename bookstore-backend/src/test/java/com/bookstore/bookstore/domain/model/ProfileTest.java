package com.bookstore.bookstore.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void constructor_allowsNullProfileFields() {
        Profile profile = new Profile(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null,
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );

        assertNull(profile.getLastName());
        assertNull(profile.getFirstName());
        assertNull(profile.getAvatarUrl());
        assertNull(profile.getGender());
        assertNull(profile.getDateOfBirth());
    }

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
    void updateProfileInfo_rejectsNullLastName() {
        Profile profile = new Profile(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "last",
                "first",
                "avatar",
                Gender.MALE,
                LocalDate.of(2000, 1, 1),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );

        DomainException exception = assertThrows(
                DomainException.class,
                () -> profile.updateProfileInfo(null, "first", null, Gender.MALE, LocalDate.of(2000, 1, 1))
        );

        assertEquals(DomainErrorCode.INVALID_PROFILE_LAST_NAME, exception.getErrorCode());
    }

    @Test
    void updateProfileInfo_rejectsNullGender() {
        Profile profile = new Profile(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "last",
                "first",
                "avatar",
                Gender.MALE,
                LocalDate.of(2000, 1, 1),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );

        DomainException exception = assertThrows(
                DomainException.class,
                () -> profile.updateProfileInfo("last", "first", null, null, LocalDate.of(2000, 1, 1))
        );

        assertEquals(DomainErrorCode.INVALID_PROFILE_GENDER, exception.getErrorCode());
    }

    @Test
    void updateProfileInfo_rejectsNullDateOfBirth() {
        Profile profile = new Profile(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "last",
                "first",
                "avatar",
                Gender.MALE,
                LocalDate.of(2000, 1, 1),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );

        DomainException exception = assertThrows(
                DomainException.class,
                () -> profile.updateProfileInfo("last", "first", null, Gender.MALE, null)
        );

        assertEquals(DomainErrorCode.INVALID_PROFILE_DATE_OF_BIRTH, exception.getErrorCode());
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
