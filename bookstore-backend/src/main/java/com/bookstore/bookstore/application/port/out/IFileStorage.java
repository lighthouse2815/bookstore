package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.SignedUrlResult;
import com.bookstore.bookstore.application.result.StorageObjectMetadataResult;
import com.bookstore.bookstore.application.result.StoragePresignResult;
import java.time.Duration;
import java.util.Optional;

public interface IFileStorage {

    StoragePresignResult createPresignedUploadUrl(
            String bucket,
            String storageKey,
            String contentType,
            Duration expiresIn
    );

    SignedUrlResult createPresignedDownloadUrl(
            String bucket,
            String storageKey,
            String originalName,
            Duration expiresIn,
            boolean attachment
    );

    void deleteObject(String bucket, String storageKey);

    boolean objectExists(String bucket, String storageKey);

    Optional<StorageObjectMetadataResult> getObjectMetadata(String bucket, String storageKey);
}
