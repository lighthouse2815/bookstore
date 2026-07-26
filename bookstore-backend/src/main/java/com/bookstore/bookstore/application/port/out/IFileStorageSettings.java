package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.enums.FileProvider;

public interface IFileStorageSettings {

    String provider();

    String bucket();

    String region();

    String endpoint();

    String accessKey();

    String secretKey();

    String publicBaseUrl();

    long presignUploadExpireMinutes();

    long presignDownloadExpireMinutes();

    long maxImageSizeMb();

    long maxDigitalFileSizeMb();

    long safetyMaxTotalBytes();

    long safetyMaxMonthlyUploads();

    default boolean isConfigured() {
        return hasText(bucket()) && hasText(accessKey()) && hasText(secretKey());
    }

    default FileProvider resolvedProvider() {
        return hasText(provider()) ? FileProvider.valueOf(provider().trim().toUpperCase()) : FileProvider.R2;
    }

    default String resolvedRegion() {
        return hasText(region()) ? region().trim() : "auto";
    }

    default String normalizedPublicBaseUrl() {
        if (!hasText(publicBaseUrl())) {
            return null;
        }

        String normalized = publicBaseUrl().trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    default long resolvedPresignUploadExpireMinutes() {
        return presignUploadExpireMinutes() > 0 ? presignUploadExpireMinutes() : 10L;
    }

    default long resolvedPresignDownloadExpireMinutes() {
        return presignDownloadExpireMinutes() > 0 ? presignDownloadExpireMinutes() : 5L;
    }

    default long resolvedMaxImageSizeMb() {
        return maxImageSizeMb() > 0 ? maxImageSizeMb() : 5L;
    }

    default long resolvedMaxDigitalFileSizeMb() {
        return maxDigitalFileSizeMb() > 0 ? maxDigitalFileSizeMb() : 200L;
    }

    default long resolvedSafetyMaxTotalBytes() {
        return safetyMaxTotalBytes() > 0 ? safetyMaxTotalBytes() : 8L * 1024L * 1024L * 1024L;
    }

    default long resolvedSafetyMaxMonthlyUploads() {
        return safetyMaxMonthlyUploads() > 0 ? safetyMaxMonthlyUploads() : 500_000L;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
