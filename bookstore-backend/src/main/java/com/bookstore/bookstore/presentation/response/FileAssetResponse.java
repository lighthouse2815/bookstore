package com.bookstore.bookstore.presentation.response;

import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import java.time.Instant;
import java.util.UUID;

public record FileAssetResponse(
        UUID id,
        FileProvider provider,
        FilePurpose purpose,
        String bucket,
        String storageKey,
        String publicUrl,
        String originalName,
        String contentType,
        Long sizeBytes,
        String checksumSha256,
        FileVisibility visibility,
        FileStatus status,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
}
