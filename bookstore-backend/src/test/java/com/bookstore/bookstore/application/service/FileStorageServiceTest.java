package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookstore.bookstore.infrastructure.storage.FileStorageProperties;
import com.bookstore.bookstore.infrastructure.storage.S3CompatibleFileStorage;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileStorageServiceTest {

    private S3CompatibleFileStorage fileStorage;

    @BeforeEach
    void setUp() {
        fileStorage = new S3CompatibleFileStorage(new FileStorageProperties(
                "r2",
                "bookstore-assets",
                "auto",
                "https://storage.example.com",
                "access-key",
                "secret-key",
                "https://cdn.example.com",
                10L,
                5L,
                5L,
                200L,
                8L * 1024L * 1024L * 1024L,
                500_000L
        ));
    }

    @Test
    void createPresignedUploadUrl_returnsPutRequestWithContentTypeHeader() {
        var result = fileStorage.createPresignedUploadUrl(
                "bookstore-assets",
                "public/books/book-1/cover.jpg",
                "image/jpeg",
                Duration.ofMinutes(10)
        );

        assertEquals("PUT", result.method());
        assertEquals("image/jpeg", result.headers().get("Content-Type"));
        assertTrue(result.url().contains("X-Amz-Signature"));
        assertTrue(result.url().contains("/bookstore-assets/"));
    }

    @Test
    void createPresignedDownloadUrl_forAttachmentIncludesContentDisposition() {
        var result = fileStorage.createPresignedDownloadUrl(
                "bookstore-assets",
                "private/digital-assets/book-1/main/ebook.pdf",
                "ebook.pdf",
                Duration.ofMinutes(5),
                true
        );

        assertTrue(result.url().contains("response-content-disposition"));
        assertTrue(result.url().contains("attachment"));
        assertTrue(result.url().contains("/bookstore-assets/"));
    }
}
