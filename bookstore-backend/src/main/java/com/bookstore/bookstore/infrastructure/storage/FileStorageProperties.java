package com.bookstore.bookstore.infrastructure.storage;

import com.bookstore.bookstore.domain.enums.FileProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public record FileStorageProperties(
        String provider,
        String bucket,
        String region,
        String endpoint,
        String accessKey,
        String secretKey,
        String publicBaseUrl,
        long presignUploadExpireMinutes,
        long presignDownloadExpireMinutes,
        long maxImageSizeMb,
        long maxDigitalFileSizeMb
) {

    public boolean isConfigured() {
        return hasText(bucket) && hasText(accessKey) && hasText(secretKey);
    }

    public FileProvider resolvedProvider() {
        return hasText(provider) ? FileProvider.valueOf(provider.trim().toUpperCase()) : FileProvider.R2;
    }

    public String resolvedRegion() {
        return hasText(region) ? region.trim() : "auto";
    }

    public String normalizedPublicBaseUrl() {
        if (!hasText(publicBaseUrl)) {
            return null;
        }

        String normalized = publicBaseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    public long resolvedPresignUploadExpireMinutes() {
        return presignUploadExpireMinutes > 0 ? presignUploadExpireMinutes : 10L;
    }

    public long resolvedPresignDownloadExpireMinutes() {
        return presignDownloadExpireMinutes > 0 ? presignDownloadExpireMinutes : 5L;
    }

    public long resolvedMaxImageSizeMb() {
        return maxImageSizeMb > 0 ? maxImageSizeMb : 5L;
    }

    public long resolvedMaxDigitalFileSizeMb() {
        return maxDigitalFileSizeMb > 0 ? maxDigitalFileSizeMb : 200L;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
