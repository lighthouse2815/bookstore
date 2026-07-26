package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.CreateDigitalAssetCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.domain.model.FileAsset;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DigitalAssetServiceTest {

    @Mock
    private IDigitalAssetRepository digitalAssetRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private FileAssetPolicyService fileAssetPolicyService;

    private DigitalAssetService digitalAssetService;

    @BeforeEach
    void setUp() {
        digitalAssetService = new DigitalAssetService(
                digitalAssetRepository,
                bookRepository,
                fileAssetPolicyService
        );
    }

    @Test
    void create_withFileAssets_derivesMetadataAndFlags() {
        UUID bookId = UUID.randomUUID();
        UUID mainFileAssetId = UUID.randomUUID();
        UUID sampleFileAssetId = UUID.randomUUID();
        FileAsset mainFileAsset = privateFileAsset(
                mainFileAssetId,
                FilePurpose.EBOOK_FILE,
                "ebook.pdf",
                "private/main/ebook.pdf"
        );
        FileAsset sampleFileAsset = privateFileAsset(
                sampleFileAssetId,
                FilePurpose.SAMPLE_FILE,
                "sample.pdf",
                "private/sample/sample.pdf"
        );
        CreateDigitalAssetCommand command = new CreateDigitalAssetCommand(
                bookId,
                DigitalAssetFormat.PDF,
                "Bản PDF",
                mainFileAssetId,
                sampleFileAssetId,
                new BigDecimal("5.00"),
                true,
                true,
                true
        );

        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book(bookId)));
        when(fileAssetPolicyService.requireActiveAsset(
                mainFileAssetId,
                FilePurpose.EBOOK_FILE,
                FileVisibility.PRIVATE
        )).thenReturn(mainFileAsset);
        when(fileAssetPolicyService.requireActiveAsset(
                sampleFileAssetId,
                FilePurpose.SAMPLE_FILE,
                FileVisibility.PRIVATE
        )).thenReturn(sampleFileAsset);
        when(digitalAssetRepository.save(org.mockito.ArgumentMatchers.any(DigitalAsset.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DigitalAsset result = digitalAssetService.create(command);

        assertEquals(mainFileAssetId, result.getFileAssetId());
        assertEquals(sampleFileAssetId, result.getSampleFileAssetId());
        assertEquals("ebook.pdf", result.getFileName());
        assertEquals("application/pdf", result.getMimeType());
        assertEquals(1_024L, result.getFileSize());
        assertEquals("private/main/ebook.pdf", result.getStorageKey());
        assertEquals("private/sample/sample.pdf", result.getSampleStorageKey());
        assertEquals(true, result.isDownloadAllowed());
        assertEquals(true, result.isPurchaseAllowed());
        assertEquals(true, result.isPublished());
    }

    @Test
    void create_withMissingBook_rejectsBookNotFound() {
        UUID bookId = UUID.randomUUID();
        CreateDigitalAssetCommand command = new CreateDigitalAssetCommand(
                bookId,
                DigitalAssetFormat.PDF,
                "Bản PDF",
                UUID.randomUUID(),
                null,
                new BigDecimal("5.00"),
                true,
                true,
                true
        );

        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> digitalAssetService.create(command)
        );

        assertEquals(ApplicationErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());
    }

    private static Book book(UUID bookId) {
        Instant now = Instant.EPOCH;
        return new Book(
                bookId,
                "Book Title",
                "ISBN-123",
                "Book Description",
                new BigDecimal("10.00"),
                10,
                List.of(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }

    private static FileAsset privateFileAsset(
            UUID fileAssetId,
            FilePurpose purpose,
            String originalName,
            String storageKey
    ) {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                fileAssetId,
                FileProvider.R2,
                purpose,
                "private-bucket",
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
