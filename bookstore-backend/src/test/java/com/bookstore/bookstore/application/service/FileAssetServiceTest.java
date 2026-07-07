package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.CompleteFileUploadCommand;
import com.bookstore.bookstore.application.command.PresignUploadCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IFileAssetRepository;
import com.bookstore.bookstore.application.port.out.IFileStorage;
import com.bookstore.bookstore.application.result.PresignedUploadResult;
import com.bookstore.bookstore.application.result.StorageObjectMetadataResult;
import com.bookstore.bookstore.application.result.StoragePresignResult;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.infrastructure.storage.FileStorageProperties;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileAssetServiceTest {

    @Mock
    private IFileAssetRepository fileAssetRepository;

    @Mock
    private IFileStorage fileStorage;

    private FileAssetService fileAssetService;

    @BeforeEach
    void setUp() {
        fileAssetService = new FileAssetService(
                fileAssetRepository,
                fileStorage,
                new FileStorageProperties(
                        "r2",
                        "bookstore-assets",
                        "auto",
                        "https://storage.example.com",
                        "key",
                        "secret",
                        "https://cdn.example.com",
                        10L,
                        5L,
                        5L,
                        200L
                )
        );
    }

    @Test
    void createPresignedUpload_createsPendingFileAsset() {
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        PresignUploadCommand command = new PresignUploadCommand(
                requesterId,
                true,
                FilePurpose.BOOK_IMAGE,
                FileVisibility.PUBLIC,
                "cover.jpg",
                "image/jpeg",
                1_024L,
                bookId,
                null,
                null,
                null,
                null
        );

        when(fileAssetRepository.save(any(FileAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileStorage.createPresignedUploadUrl(
                eq("bookstore-assets"),
                any(String.class),
                any(String.class),
                any()
        )).thenReturn(new StoragePresignResult(
                "https://storage.example.com/upload",
                "PUT",
                Map.of("Content-Type", "image/jpeg"),
                Instant.parse("2026-06-25T00:10:00Z")
        ));

        PresignedUploadResult result = fileAssetService.createPresignedUpload(command);

        ArgumentCaptor<FileAsset> captor = ArgumentCaptor.forClass(FileAsset.class);
        verify(fileAssetRepository).save(captor.capture());
        FileAsset savedAsset = captor.getValue();
        assertEquals(FileStatus.PENDING, savedAsset.getStatus());
        assertEquals(FilePurpose.BOOK_IMAGE, savedAsset.getPurpose());
        assertEquals(FileVisibility.PUBLIC, savedAsset.getVisibility());
        assertEquals("https://cdn.example.com/" + savedAsset.getStorageKey(), savedAsset.getPublicUrl());
        assertNotNull(result.fileAssetId());
        assertEquals(savedAsset.getStorageKey(), result.storageKey());
    }

    @Test
    void createPresignedUpload_whenUserUploadsAdminOnlyPurpose_rejectsAccessDenied() {
        UUID requesterId = UUID.randomUUID();
        List<FilePurpose> adminOnlyPurposes = List.of(
                FilePurpose.BOOK_IMAGE,
                FilePurpose.AUTHOR_AVATAR,
                FilePurpose.EBOOK_FILE,
                FilePurpose.SAMPLE_FILE
        );

        for (FilePurpose purpose : adminOnlyPurposes) {
            ApplicationException exception = assertThrows(
                    ApplicationException.class,
                    () -> fileAssetService.createPresignedUpload(nonAdminCommand(requesterId, purpose))
            );

            assertEquals(ApplicationErrorCode.FILE_ASSET_ACCESS_DENIED, exception.getErrorCode());
        }
    }

    @Test
    void createPresignedUpload_whenUserAvatar_usesRequesterScopedStorageKey() {
        UUID requesterId = UUID.randomUUID();
        PresignUploadCommand command = new PresignUploadCommand(
                requesterId,
                false,
                FilePurpose.USER_AVATAR,
                FileVisibility.PUBLIC,
                "avatar.jpg",
                "image/jpeg",
                1_024L,
                null,
                null,
                null,
                null,
                null
        );

        when(fileAssetRepository.save(any(FileAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileStorage.createPresignedUploadUrl(
                eq("bookstore-assets"),
                any(String.class),
                any(String.class),
                any()
        )).thenReturn(new StoragePresignResult(
                "https://storage.example.com/upload",
                "PUT",
                Map.of("Content-Type", "image/jpeg"),
                Instant.parse("2026-06-25T00:10:00Z")
        ));

        fileAssetService.createPresignedUpload(command);

        ArgumentCaptor<FileAsset> captor = ArgumentCaptor.forClass(FileAsset.class);
        verify(fileAssetRepository).save(captor.capture());
        assertEquals(FilePurpose.USER_AVATAR, captor.getValue().getPurpose());
        assertEquals("public/users/" + requesterId + "/avatar/", captor.getValue().getStorageKey().substring(
                0,
                ("public/users/" + requesterId + "/avatar/").length()
        ));
        assertEquals(requesterId, captor.getValue().getCreatedBy());
    }

    @Test
    void completeUpload_whenObjectExists_activatesFileAsset() {
        UUID requesterId = UUID.randomUUID();
        UUID fileAssetId = UUID.randomUUID();
        FileAsset pendingAsset = pendingBookImage(fileAssetId, requesterId);

        when(fileAssetRepository.findByIdIncludingDeleted(fileAssetId)).thenReturn(Optional.of(pendingAsset));
        when(fileStorage.getObjectMetadata("bookstore-assets", pendingAsset.getStorageKey())).thenReturn(
                Optional.of(new StorageObjectMetadataResult("image/jpeg", 1_024L, "etag"))
        );
        when(fileAssetRepository.save(any(FileAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileAsset result = fileAssetService.completeUpload(new CompleteFileUploadCommand(
                requesterId,
                false,
                fileAssetId,
                "sha256-checksum"
        ));

        assertEquals(FileStatus.ACTIVE, result.getStatus());
        assertEquals("sha256-checksum", result.getChecksumSha256());
    }

    @Test
    void completeUpload_whenMetadataSizeMismatch_rejectsActivation() {
        UUID requesterId = UUID.randomUUID();
        UUID fileAssetId = UUID.randomUUID();
        FileAsset pendingAsset = pendingBookImage(fileAssetId, requesterId);

        when(fileAssetRepository.findByIdIncludingDeleted(fileAssetId)).thenReturn(Optional.of(pendingAsset));
        when(fileStorage.getObjectMetadata("bookstore-assets", pendingAsset.getStorageKey())).thenReturn(
                Optional.of(new StorageObjectMetadataResult("image/jpeg", 2_048L, "etag"))
        );

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> fileAssetService.completeUpload(new CompleteFileUploadCommand(
                        requesterId,
                        false,
                        fileAssetId,
                        null
                ))
        );

        assertEquals(ApplicationErrorCode.FILE_ASSET_OBJECT_METADATA_MISMATCH, exception.getErrorCode());
    }

    @Test
    void completeUpload_whenMetadataContentTypeMismatch_rejectsActivation() {
        UUID requesterId = UUID.randomUUID();
        UUID fileAssetId = UUID.randomUUID();
        FileAsset pendingAsset = pendingBookImage(fileAssetId, requesterId);

        when(fileAssetRepository.findByIdIncludingDeleted(fileAssetId)).thenReturn(Optional.of(pendingAsset));
        when(fileStorage.getObjectMetadata("bookstore-assets", pendingAsset.getStorageKey())).thenReturn(
                Optional.of(new StorageObjectMetadataResult("image/png", 1_024L, "etag"))
        );

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> fileAssetService.completeUpload(new CompleteFileUploadCommand(
                        requesterId,
                        false,
                        fileAssetId,
                        null
                ))
        );

        assertEquals(ApplicationErrorCode.FILE_ASSET_OBJECT_METADATA_MISMATCH, exception.getErrorCode());
    }

    @Test
    void createPresignedUpload_whenFilenameExtensionDoesNotMatchContentType_usesContentTypeExtension() {
        PresignUploadCommand command = new PresignUploadCommand(
                UUID.randomUUID(),
                false,
                FilePurpose.USER_AVATAR,
                FileVisibility.PUBLIC,
                "avatar.exe",
                "image/png",
                1_024L,
                null,
                null,
                null,
                null,
                null
        );

        when(fileAssetRepository.save(any(FileAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileStorage.createPresignedUploadUrl(
                eq("bookstore-assets"),
                any(String.class),
                eq("image/png"),
                any()
        )).thenReturn(new StoragePresignResult(
                "https://storage.example.com/upload",
                "PUT",
                Map.of("Content-Type", "image/png"),
                Instant.parse("2026-06-25T00:10:00Z")
        ));

        fileAssetService.createPresignedUpload(command);

        ArgumentCaptor<FileAsset> captor = ArgumentCaptor.forClass(FileAsset.class);
        verify(fileAssetRepository).save(captor.capture());
        assertEquals(true, captor.getValue().getStorageKey().endsWith(".png"));
    }

    @Test
    void createPresignedUpload_whenFileNameContainsPathTraversal_storageKeyIgnoresClientPath() {
        PresignUploadCommand command = new PresignUploadCommand(
                UUID.randomUUID(),
                false,
                FilePurpose.USER_AVATAR,
                FileVisibility.PUBLIC,
                "..\\..\\payload.png",
                "image/png",
                1_024L,
                null,
                null,
                null,
                null,
                null
        );

        when(fileAssetRepository.save(any(FileAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileStorage.createPresignedUploadUrl(
                eq("bookstore-assets"),
                any(String.class),
                eq("image/png"),
                any()
        )).thenReturn(new StoragePresignResult(
                "https://storage.example.com/upload",
                "PUT",
                Map.of("Content-Type", "image/png"),
                Instant.parse("2026-06-25T00:10:00Z")
        ));

        fileAssetService.createPresignedUpload(command);

        ArgumentCaptor<FileAsset> captor = ArgumentCaptor.forClass(FileAsset.class);
        verify(fileAssetRepository).save(captor.capture());
        String storageKey = captor.getValue().getStorageKey();
        assertEquals(false, storageKey.contains(".."));
        assertEquals(false, storageKey.contains("payload"));
    }

    @Test
    void delete_whenFileAssetIsUsedByBookImages_rejectsDelete() {
        assertDeleteBlockedByUsage(List.of("book_images.file_asset_id"));
    }

    @Test
    void delete_whenFileAssetIsUsedByProfiles_rejectsDelete() {
        assertDeleteBlockedByUsage(List.of("profiles.avatar_file_asset_id"));
    }

    @Test
    void delete_whenFileAssetIsUsedByDigitalAssets_rejectsDelete() {
        assertDeleteBlockedByUsage(List.of(
                "digital_assets.file_asset_id",
                "digital_assets.sample_file_asset_id"
        ));
    }

    @Test
    void delete_whenFileAssetIsUnused_softDeletesAndRemovesObject() {
        UUID requesterId = UUID.randomUUID();
        UUID fileAssetId = UUID.randomUUID();
        FileAsset activeAsset = activeBookImage(fileAssetId, requesterId);

        when(fileAssetRepository.findByIdIncludingDeleted(fileAssetId)).thenReturn(Optional.of(activeAsset));
        when(fileAssetRepository.findUsageReferences(fileAssetId)).thenReturn(List.of());
        when(fileStorage.objectExists("bookstore-assets", activeAsset.getStorageKey())).thenReturn(true);
        when(fileAssetRepository.save(any(FileAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fileAssetService.delete(fileAssetId, requesterId, false);

        verify(fileStorage).deleteObject("bookstore-assets", activeAsset.getStorageKey());
        verify(fileAssetRepository).save(activeAsset);
        assertEquals(FileStatus.DELETED, activeAsset.getStatus());
    }

    @Test
    void createPresignedUpload_whenContentTypeIsInvalid_rejectsUpload() {
        PresignUploadCommand command = new PresignUploadCommand(
                UUID.randomUUID(),
                true,
                FilePurpose.BOOK_IMAGE,
                FileVisibility.PUBLIC,
                "cover.pdf",
                "application/pdf",
                1_024L,
                UUID.randomUUID(),
                null,
                null,
                null,
                null
        );

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> fileAssetService.createPresignedUpload(command)
        );

        assertEquals(ApplicationErrorCode.FILE_ASSET_CONTENT_TYPE_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    void createPresignedUpload_whenSizeExceedsLimit_rejectsUpload() {
        PresignUploadCommand command = new PresignUploadCommand(
                UUID.randomUUID(),
                true,
                FilePurpose.BOOK_IMAGE,
                FileVisibility.PUBLIC,
                "cover.jpg",
                "image/jpeg",
                6L * 1024L * 1024L,
                UUID.randomUUID(),
                null,
                null,
                null,
                null
        );

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> fileAssetService.createPresignedUpload(command)
        );

        assertEquals(ApplicationErrorCode.FILE_ASSET_SIZE_EXCEEDED, exception.getErrorCode());
    }

    private static FileAsset pendingBookImage(UUID fileAssetId, UUID createdBy) {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                fileAssetId,
                FileProvider.R2,
                FilePurpose.BOOK_IMAGE,
                "bookstore-assets",
                "public/books/book-1/cover.jpg",
                "https://cdn.example.com/public/books/book-1/cover.jpg",
                "cover.jpg",
                "image/jpeg",
                1_024L,
                null,
                FileVisibility.PUBLIC,
                FileStatus.PENDING,
                createdBy,
                now,
                now,
                null
        );
    }

    private static FileAsset activeBookImage(UUID fileAssetId, UUID createdBy) {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                fileAssetId,
                FileProvider.R2,
                FilePurpose.BOOK_IMAGE,
                "bookstore-assets",
                "public/books/book-1/cover.jpg",
                "https://cdn.example.com/public/books/book-1/cover.jpg",
                "cover.jpg",
                "image/jpeg",
                1_024L,
                "checksum",
                FileVisibility.PUBLIC,
                FileStatus.ACTIVE,
                createdBy,
                now,
                now,
                null
        );
    }

    private void assertDeleteBlockedByUsage(List<String> usageReferences) {
        UUID requesterId = UUID.randomUUID();
        UUID fileAssetId = UUID.randomUUID();
        FileAsset activeAsset = activeBookImage(fileAssetId, requesterId);

        when(fileAssetRepository.findByIdIncludingDeleted(fileAssetId)).thenReturn(Optional.of(activeAsset));
        when(fileAssetRepository.findUsageReferences(fileAssetId)).thenReturn(usageReferences);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> fileAssetService.delete(fileAssetId, requesterId, false)
        );

        assertEquals(ApplicationErrorCode.FILE_ASSET_IN_USE, exception.getErrorCode());
        verify(fileStorage, never()).deleteObject(any(), any());
        verify(fileStorage, never()).objectExists(any(), any());
    }

    private static PresignUploadCommand nonAdminCommand(UUID requesterId, FilePurpose purpose) {
        FileVisibility visibility = switch (purpose) {
            case BOOK_IMAGE, USER_AVATAR, AUTHOR_AVATAR, REVIEW_IMAGE -> FileVisibility.PUBLIC;
            case EBOOK_FILE, SAMPLE_FILE, INVOICE -> FileVisibility.PRIVATE;
        };
        String contentType = switch (purpose) {
            case BOOK_IMAGE, USER_AVATAR, AUTHOR_AVATAR, REVIEW_IMAGE -> "image/jpeg";
            case EBOOK_FILE, SAMPLE_FILE, INVOICE -> "application/pdf";
        };

        return new PresignUploadCommand(
                requesterId,
                false,
                purpose,
                visibility,
                "upload.bin",
                contentType,
                1_024L,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
    }
}
