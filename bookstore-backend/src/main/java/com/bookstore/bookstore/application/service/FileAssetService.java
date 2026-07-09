package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CompleteFileUploadCommand;
import com.bookstore.bookstore.application.command.PresignUploadCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IFileAssetService;
import com.bookstore.bookstore.application.port.out.IFileAssetRepository;
import com.bookstore.bookstore.application.port.out.IFileStorage;
import com.bookstore.bookstore.application.port.out.IFileStorageSettings;
import com.bookstore.bookstore.application.result.PresignedUploadResult;
import com.bookstore.bookstore.application.result.StorageObjectMetadataResult;
import com.bookstore.bookstore.application.result.StoragePresignResult;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.FileAsset;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileAssetService implements IFileAssetService {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> DIGITAL_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/epub+zip",
            "audio/mpeg",
            "audio/mp4",
            "audio/x-m4a"
    );
    private static final Set<String> INVOICE_CONTENT_TYPES = Set.of("application/pdf");

    private final IFileAssetRepository fileAssetRepository;
    private final IFileStorage fileStorage;
    private final IFileStorageSettings fileStorageSettings;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PresignedUploadResult createPresignedUpload(PresignUploadCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        requireStorageConfigured();
        requireUploadPermission(command.purpose(), command.admin());
        validateVisibility(command.purpose(), command.visibility());
        validateContentType(command.purpose(), command.contentType());
        validateSize(command.purpose(), command.sizeBytes());

        String storageKey = buildStorageKey(command);
        Instant now = Instant.now();
        FileAsset fileAsset = new FileAsset(
                UUID.randomUUID(),
                fileStorageSettings.resolvedProvider(),
                command.purpose(),
                fileStorageSettings.bucket().trim(),
                storageKey,
                buildPublicUrl(command.visibility(), storageKey),
                normalizeOriginalName(command.fileName()),
                command.contentType().trim(),
                command.sizeBytes(),
                null,
                command.visibility(),
                FileStatus.PENDING,
                command.requesterId(),
                now,
                now,
                null
        );
        FileAsset savedFileAsset = fileAssetRepository.save(fileAsset);

        StoragePresignResult presignResult = fileStorage.createPresignedUploadUrl(
                savedFileAsset.getBucket(),
                savedFileAsset.getStorageKey(),
                savedFileAsset.getContentType(),
                Duration.ofMinutes(fileStorageSettings.resolvedPresignUploadExpireMinutes())
        );

        return new PresignedUploadResult(
                savedFileAsset.getId(),
                presignResult.url(),
                presignResult.method(),
                presignResult.headers(),
                presignResult.expiresAt(),
                savedFileAsset.getStorageKey()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileAsset completeUpload(CompleteFileUploadCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        requireStorageConfigured();
        FileAsset fileAsset = loadFileAsset(command.fileAssetId());
        requireOwnership(fileAsset, command.requesterId(), command.admin());

        StorageObjectMetadataResult objectMetadata = fileStorage.getObjectMetadata(
                        fileAsset.getBucket(),
                        fileAsset.getStorageKey()
                )
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.FILE_ASSET_OBJECT_NOT_FOUND));

        validateObjectMetadata(fileAsset, objectMetadata);

        fileAsset.activate(command.checksumSha256());
        return fileAssetRepository.save(fileAsset);
    }

    @Override
    public FileAsset getById(UUID fileAssetId, UUID requesterId, boolean admin) {
        if (fileAssetId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "fileAssetId");
        }
        if (requesterId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "requesterId");
        }

        FileAsset fileAsset = loadFileAsset(fileAssetId);
        if (!fileAsset.isPublic()) {
            requireOwnership(fileAsset, requesterId, admin);
        }
        return fileAsset;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UUID fileAssetId, UUID requesterId, boolean admin) {
        if (fileAssetId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "fileAssetId");
        }
        if (requesterId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "requesterId");
        }

        FileAsset fileAsset = loadFileAsset(fileAssetId);
        requireOwnership(fileAsset, requesterId, admin);
        List<String> usageReferences = fileAssetRepository.findUsageReferences(fileAssetId);
        if (!usageReferences.isEmpty()) {
            throw new ApplicationException(
                    ApplicationErrorCode.FILE_ASSET_IN_USE,
                    String.join(", ", usageReferences)
            );
        }

        if (fileStorageSettings.isConfigured() && fileStorage.objectExists(fileAsset.getBucket(), fileAsset.getStorageKey())) {
            fileStorage.deleteObject(fileAsset.getBucket(), fileAsset.getStorageKey());
        }

        fileAsset.softDelete();
        fileAssetRepository.save(fileAsset);
    }

    private FileAsset loadFileAsset(UUID fileAssetId) {
        return fileAssetRepository.findByIdIncludingDeleted(fileAssetId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.FILE_ASSET_NOT_FOUND));
    }

    private void requireStorageConfigured() {
        if (!fileStorageSettings.isConfigured()) {
            throw new ApplicationException(ApplicationErrorCode.FILE_STORAGE_NOT_CONFIGURED);
        }
    }

    private void requireUploadPermission(FilePurpose purpose, boolean admin) {
        if (purpose == FilePurpose.USER_AVATAR || purpose == FilePurpose.REVIEW_IMAGE) {
            return;
        }

        if (!admin) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_ACCESS_DENIED);
        }
    }

    private void requireOwnership(FileAsset fileAsset, UUID requesterId, boolean admin) {
        if (admin || fileAsset.getCreatedBy().equals(requesterId)) {
            return;
        }

        throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_ACCESS_DENIED);
    }

    private void validateVisibility(FilePurpose purpose, FileVisibility visibility) {
        boolean shouldBePublic = switch (purpose) {
            case BOOK_IMAGE, USER_AVATAR, AUTHOR_AVATAR, REVIEW_IMAGE -> true;
            case EBOOK_FILE, SAMPLE_FILE, INVOICE -> false;
        };

        if (shouldBePublic && visibility != FileVisibility.PUBLIC) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_INVALID_VISIBILITY);
        }

        if (!shouldBePublic && visibility != FileVisibility.PRIVATE) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_INVALID_VISIBILITY);
        }
    }

    private void validateContentType(FilePurpose purpose, String contentType) {
        String normalizedContentType = contentType == null ? "" : contentType.trim().toLowerCase();
        Set<String> allowedTypes = switch (purpose) {
            case BOOK_IMAGE, USER_AVATAR, AUTHOR_AVATAR, REVIEW_IMAGE -> IMAGE_CONTENT_TYPES;
            case EBOOK_FILE, SAMPLE_FILE -> DIGITAL_CONTENT_TYPES;
            case INVOICE -> INVOICE_CONTENT_TYPES;
        };

        if (!allowedTypes.contains(normalizedContentType)) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_CONTENT_TYPE_NOT_ALLOWED);
        }
    }

    private void validateSize(FilePurpose purpose, Long sizeBytes) {
        long maxBytes = switch (purpose) {
            case BOOK_IMAGE, USER_AVATAR, AUTHOR_AVATAR, REVIEW_IMAGE ->
                    fileStorageSettings.resolvedMaxImageSizeMb() * 1024L * 1024L;
            case EBOOK_FILE, SAMPLE_FILE, INVOICE ->
                    fileStorageSettings.resolvedMaxDigitalFileSizeMb() * 1024L * 1024L;
        };

        if (sizeBytes == null || sizeBytes <= 0 || sizeBytes > maxBytes) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_SIZE_EXCEEDED);
        }
    }

    private String buildStorageKey(PresignUploadCommand command) {
        String extension = resolveExtension(command.contentType());
        String suffix = UUID.randomUUID() + (extension == null ? "" : "." + extension);

        return switch (command.purpose()) {
            case BOOK_IMAGE -> command.bookId() != null
                    ? "public/books/" + command.bookId() + "/" + suffix
                    : "tmp/" + command.requesterId() + "/book-images/" + suffix;
            case USER_AVATAR -> "public/users/" + command.requesterId() + "/avatar/" + suffix;
            case AUTHOR_AVATAR -> command.authorId() != null
                    ? "public/authors/" + command.authorId() + "/avatar/" + suffix
                    : "tmp/" + command.requesterId() + "/author-avatars/" + suffix;
            case REVIEW_IMAGE -> command.reviewId() != null
                    ? "public/reviews/" + command.reviewId() + "/" + suffix
                    : "tmp/" + command.requesterId() + "/review-images/" + suffix;
            case EBOOK_FILE -> command.digitalAssetId() != null
                    ? "private/digital-assets/" + command.digitalAssetId() + "/main/" + suffix
                    : "tmp/" + command.requesterId() + "/digital-assets/main/" + suffix;
            case SAMPLE_FILE -> command.digitalAssetId() != null
                    ? "private/digital-assets/" + command.digitalAssetId() + "/sample/" + suffix
                    : "tmp/" + command.requesterId() + "/digital-assets/sample/" + suffix;
            case INVOICE -> command.orderId() != null
                    ? "private/orders/" + command.orderId() + "/invoice/" + suffix
                    : "tmp/" + command.requesterId() + "/invoices/" + suffix;
        };
    }

    private String resolveExtension(String contentType) {
        String normalizedContentType = contentType.trim().toLowerCase();
        if ("image/jpeg".equals(normalizedContentType)) {
            return "jpg";
        }
        if ("image/png".equals(normalizedContentType)) {
            return "png";
        }
        if ("image/webp".equals(normalizedContentType)) {
            return "webp";
        }
        if ("application/pdf".equals(normalizedContentType)) {
            return "pdf";
        }
        if ("application/epub+zip".equals(normalizedContentType)) {
            return "epub";
        }
        if ("audio/mpeg".equals(normalizedContentType)) {
            return "mp3";
        }
        if ("audio/x-m4a".equals(normalizedContentType)) {
            return "m4a";
        }
        if ("audio/mp4".equals(normalizedContentType)) {
            return "m4a";
        }
        return null;
    }

    private String normalizeOriginalName(String fileName) {
        String normalized = fileName == null ? "" : fileName.trim().replace('\\', '/');
        int lastSlashIndex = normalized.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            normalized = normalized.substring(lastSlashIndex + 1);
        }
        return normalized;
    }

    private void validateObjectMetadata(FileAsset fileAsset, StorageObjectMetadataResult objectMetadata) {
        if (objectMetadata.contentLength() != null
                && fileAsset.getSizeBytes() != null
                && !objectMetadata.contentLength().equals(fileAsset.getSizeBytes())) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_OBJECT_METADATA_MISMATCH);
        }

        if (hasText(objectMetadata.contentType())
                && hasText(fileAsset.getContentType())
                && !areContentTypesCompatible(fileAsset.getContentType(), objectMetadata.contentType())) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_OBJECT_METADATA_MISMATCH);
        }
    }

    private boolean areContentTypesCompatible(String expectedContentType, String actualContentType) {
        return normalizeComparableContentType(expectedContentType)
                .equals(normalizeComparableContentType(actualContentType));
    }

    private String normalizeComparableContentType(String contentType) {
        return contentType.trim()
                .toLowerCase()
                .split(";", 2)[0]
                .trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String buildPublicUrl(FileVisibility visibility, String storageKey) {
        if (visibility != FileVisibility.PUBLIC) {
            return null;
        }

        String publicBaseUrl = fileStorageSettings.normalizedPublicBaseUrl();
        if (publicBaseUrl == null) {
            return null;
        }

        return publicBaseUrl + "/" + storageKey;
    }
}
