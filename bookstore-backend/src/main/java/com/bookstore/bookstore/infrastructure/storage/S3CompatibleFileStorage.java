package com.bookstore.bookstore.infrastructure.storage;

import com.bookstore.bookstore.application.port.out.IFileStorage;
import com.bookstore.bookstore.application.result.SignedUrlResult;
import com.bookstore.bookstore.application.result.StorageObjectMetadataResult;
import com.bookstore.bookstore.application.result.StoragePresignResult;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3CompatibleFileStorage implements IFileStorage {

    private final FileStorageProperties fileStorageProperties;

    private volatile S3Client s3Client;
    private volatile S3Presigner s3Presigner;

    @Override
    public StoragePresignResult createPresignedUploadUrl(
            String bucket,
            String storageKey,
            String contentType,
            Duration expiresIn
    ) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest request = getS3Presigner().presignPutObject(builder -> builder
                .signatureDuration(expiresIn)
                .putObjectRequest(putObjectRequest));

        Map<String, String> headers = new LinkedHashMap<>();
        if (contentType != null && !contentType.isBlank()) {
            headers.put("Content-Type", contentType);
        }

        return new StoragePresignResult(
                request.url().toString(),
                "PUT",
                Map.copyOf(headers),
                request.expiration()
        );
    }

    @Override
    public SignedUrlResult createPresignedDownloadUrl(
            String bucket,
            String storageKey,
            String originalName,
            Duration expiresIn,
            boolean attachment
    ) {
        GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey);

        String normalizedFileName = normalizeFileName(originalName);
        if (normalizedFileName != null) {
            requestBuilder.responseContentDisposition(buildContentDisposition(normalizedFileName, attachment));
        }

        PresignedGetObjectRequest request = getS3Presigner().presignGetObject(builder -> builder
                .signatureDuration(expiresIn)
                .getObjectRequest(requestBuilder.build()));

        return new SignedUrlResult(request.url().toString(), request.expiration());
    }

    @Override
    public void deleteObject(String bucket, String storageKey) {
        getS3Client().deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build());
    }

    @Override
    public boolean objectExists(String bucket, String storageKey) {
        try {
            getS3Client().headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .build());
            return true;
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return false;
            }
            throw exception;
        }
    }

    @Override
    public Optional<StorageObjectMetadataResult> getObjectMetadata(String bucket, String storageKey) {
        try {
            HeadObjectResponse response = getS3Client().headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .build());
            return Optional.of(new StorageObjectMetadataResult(
                    response.contentType(),
                    response.contentLength(),
                    response.eTag()
            ));
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return Optional.empty();
            }
            throw exception;
        }
    }

    private S3Client getS3Client() {
        if (s3Client == null) {
            synchronized (this) {
                if (s3Client == null) {
                    s3Client = S3Client.builder()
                            .region(Region.of(fileStorageProperties.resolvedRegion()))
                            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                                    fileStorageProperties.accessKey(),
                                    fileStorageProperties.secretKey()
                            )))
                            .serviceConfiguration(buildS3Configuration())
                            .endpointOverride(resolveEndpoint())
                            .build();
                }
            }
        }
        return s3Client;
    }

    private S3Presigner getS3Presigner() {
        if (s3Presigner == null) {
            synchronized (this) {
                if (s3Presigner == null) {
                    s3Presigner = S3Presigner.builder()
                            .region(Region.of(fileStorageProperties.resolvedRegion()))
                            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                                    fileStorageProperties.accessKey(),
                                    fileStorageProperties.secretKey()
                            )))
                            .serviceConfiguration(buildS3Configuration())
                            .endpointOverride(resolveEndpoint())
                            .build();
                }
            }
        }
        return s3Presigner;
    }

    private URI resolveEndpoint() {
        if (fileStorageProperties.endpoint() == null || fileStorageProperties.endpoint().isBlank()) {
            return URI.create("https://s3.amazonaws.com");
        }
        return URI.create(fileStorageProperties.endpoint().trim());
    }

    private String normalizeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return null;
        }

        String normalized = originalName.trim().replace('\\', '/');
        int lastSlashIndex = normalized.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            normalized = normalized.substring(lastSlashIndex + 1);
        }
        return normalized.isBlank() ? null : normalized.replace("\"", "");
    }

    private S3Configuration buildS3Configuration() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }

    private String buildContentDisposition(String fileName, boolean attachment) {
        return (attachment ? "attachment" : "inline") + "; filename=\"" + fileName + "\"";
    }
}
