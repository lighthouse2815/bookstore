package com.bookstore.bookstore.presentation.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.domain.model.FileAsset;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DigitalLibraryWebMapperTest {

    private final DigitalLibraryWebMapper mapper = new DigitalLibraryWebMapper();

    @Test
    void toPublishedDigitalAssetResponse_returnsSafePublicShape() {
        DigitalAsset asset = digitalAsset(true, true, true);

        var response = mapper.toPublishedDigitalAssetResponse(asset);

        assertEquals(asset.getId(), response.id());
        assertEquals(asset.getBookId(), response.bookId());
        assertEquals(asset.getFormat(), response.format());
        assertEquals(asset.getTitle(), response.title());
        assertEquals(asset.getPrice(), response.price());
        assertTrue(response.downloadAllowed());
        assertTrue(response.purchaseAllowed());
        assertTrue(response.sampleAvailable());
    }

    @Test
    void toDigitalAssetResponse_returnsAdminMetadataAndPurchaseFlag() {
        DigitalAsset asset = digitalAsset(true, true, true);

        var response = mapper.toDigitalAssetResponse(asset);

        assertEquals(asset.getFileAssetId(), response.fileAssetId());
        assertEquals(asset.getSampleFileAssetId(), response.sampleFileAssetId());
        assertEquals(asset.getChecksum(), response.checksum());
        assertTrue(response.downloadAllowed());
        assertTrue(response.purchaseAllowed());
        assertTrue(response.published());
    }

    private static DigitalAsset digitalAsset(
            boolean downloadAllowed,
            boolean purchaseAllowed,
            boolean published
    ) {
        Instant now = Instant.EPOCH;
        return new DigitalAsset(
                UUID.randomUUID(),
                UUID.randomUUID(),
                DigitalAssetFormat.PDF,
                "Bản PDF",
                fileAsset(FilePurpose.EBOOK_FILE, "private/digital/main/ebook.pdf", "ebook.pdf"),
                fileAsset(FilePurpose.SAMPLE_FILE, "private/digital/sample/sample.pdf", "sample.pdf"),
                new BigDecimal("5.00"),
                downloadAllowed,
                purchaseAllowed,
                published,
                now,
                now,
                null
        );
    }

    private static FileAsset fileAsset(FilePurpose purpose, String storageKey, String originalName) {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                UUID.randomUUID(),
                FileProvider.R2,
                purpose,
                "bookstore-assets",
                storageKey,
                null,
                originalName,
                "application/pdf",
                1_024L,
                "checksum",
                FileVisibility.PRIVATE,
                FileStatus.ACTIVE,
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }
}
