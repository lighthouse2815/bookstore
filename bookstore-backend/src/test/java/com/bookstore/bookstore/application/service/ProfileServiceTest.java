package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.UpdateProfileCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IProfileRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.enums.Gender;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.domain.model.Profile;
import com.bookstore.bookstore.domain.model.Role;
import com.bookstore.bookstore.domain.model.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private IProfileRepository profileRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private FileAssetPolicyService fileAssetPolicyService;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileRepository, userRepository, fileAssetPolicyService);
    }

    @Test
    void update_withAvatarFileAsset_updatesAvatarFieldsFromFileAsset() {
        UUID userId = UUID.randomUUID();
        UUID avatarFileAssetId = UUID.randomUUID();
        Profile currentProfile = profile(userId, null);
        FileAsset avatarFileAsset = publicAvatarFile(avatarFileAssetId);

        when(userRepository.findByIdActive(userId)).thenReturn(Optional.of(user(userId)));
        when(profileRepository.findByUserIdActive(userId)).thenReturn(Optional.of(currentProfile));
        when(fileAssetPolicyService.requireActiveOwnedAsset(
                avatarFileAssetId,
                FilePurpose.USER_AVATAR,
                FileVisibility.PUBLIC,
                userId
        )).thenReturn(avatarFileAsset);
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Profile result = profileService.update(new UpdateProfileCommand(
                userId,
                "Nguyen",
                "An",
                avatarFileAssetId,
                Gender.MALE,
                LocalDate.of(2000, 1, 1)
        ));

        assertEquals(avatarFileAssetId, result.getAvatarFileAssetId());
        assertEquals("https://cdn.example.com/public/users/avatar.jpg", result.getAvatarUrl());
    }

    @Test
    void update_whenKeepingCurrentAdminAssignedAvatar_skipsOwnershipCheck() {
        UUID userId = UUID.randomUUID();
        UUID avatarFileAssetId = UUID.randomUUID();
        Profile currentProfile = profile(userId, publicAvatarFile(avatarFileAssetId));

        when(userRepository.findByIdActive(userId)).thenReturn(Optional.of(user(userId)));
        when(profileRepository.findByUserIdActive(userId)).thenReturn(Optional.of(currentProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Profile result = profileService.update(new UpdateProfileCommand(
                userId,
                "Le",
                "Lan",
                avatarFileAssetId,
                Gender.FEMALE,
                LocalDate.of(1997, 4, 15)
        ));

        assertEquals(avatarFileAssetId, result.getAvatarFileAssetId());
        verifyNoInteractions(fileAssetPolicyService);
    }

    @Test
    void update_withNullAvatarFileAsset_clearsCurrentAvatar() {
        UUID userId = UUID.randomUUID();
        Profile currentProfile = profile(userId, publicAvatarFile(UUID.randomUUID()));

        when(userRepository.findByIdActive(userId)).thenReturn(Optional.of(user(userId)));
        when(profileRepository.findByUserIdActive(userId)).thenReturn(Optional.of(currentProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Profile result = profileService.update(new UpdateProfileCommand(
                userId,
                "Tran",
                "Binh",
                null,
                Gender.FEMALE,
                LocalDate.of(1999, 5, 20)
        ));

        assertNull(result.getAvatarFileAssetId());
        assertNull(result.getAvatarUrl());
    }

    @Test
    void update_whenSwitchingToAvatarOwnedByOtherUser_rejectsAccessDenied() {
        UUID userId = UUID.randomUUID();
        UUID requestedAvatarFileAssetId = UUID.randomUUID();
        Profile currentProfile = profile(userId, null);

        when(userRepository.findByIdActive(userId)).thenReturn(Optional.of(user(userId)));
        when(profileRepository.findByUserIdActive(userId)).thenReturn(Optional.of(currentProfile));
        when(fileAssetPolicyService.requireActiveOwnedAsset(
                requestedAvatarFileAssetId,
                FilePurpose.USER_AVATAR,
                FileVisibility.PUBLIC,
                userId
        )).thenThrow(new ApplicationException(ApplicationErrorCode.FILE_ASSET_ACCESS_DENIED));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> profileService.update(new UpdateProfileCommand(
                        userId,
                        "Pham",
                        "Minh",
                        requestedAvatarFileAssetId,
                        Gender.OTHER,
                        LocalDate.of(1995, 7, 1)
                ))
        );

        assertEquals(ApplicationErrorCode.FILE_ASSET_ACCESS_DENIED, exception.getErrorCode());
    }

    private static Profile profile(UUID userId, FileAsset avatarFileAsset) {
        Instant now = Instant.EPOCH;
        return new Profile(
                UUID.randomUUID(),
                userId,
                "Tran",
                "An",
                avatarFileAsset,
                Gender.OTHER,
                LocalDate.of(1998, 1, 1),
                now,
                now,
                null
        );
    }

    private static User user(UUID userId) {
        Instant now = Instant.EPOCH;
        return new User(
                userId,
                "user",
                "hash",
                "0123456789",
                "user@example.com",
                UserStatus.ACTIVE,
                false,
                Set.of(new Role(UUID.randomUUID(), "USER", "Default role", Set.of(), now, now, null)),
                now,
                now,
                null
        );
    }

    private static FileAsset publicAvatarFile(UUID fileAssetId) {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                fileAssetId,
                FileProvider.R2,
                FilePurpose.USER_AVATAR,
                "bookstore-assets",
                "public/users/avatar.jpg",
                "https://cdn.example.com/public/users/avatar.jpg",
                "avatar.jpg",
                "image/jpeg",
                2_048L,
                "checksum",
                FileVisibility.PUBLIC,
                FileStatus.ACTIVE,
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }
}
