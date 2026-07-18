package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.UpdateReadingProgressCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.application.port.out.IFileStorage;
import com.bookstore.bookstore.application.port.out.IReadingProgressRepository;
import com.bookstore.bookstore.application.port.out.IUserDigitalAccessRepository;
import com.bookstore.bookstore.application.result.SignedUrlResult;
import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.enums.DigitalAccessType;
import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.enums.PaymentMethod;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.UserDigitalAccess;
import com.bookstore.bookstore.infrastructure.storage.FileStorageProperties;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DigitalLibraryServiceTest {

    @Mock
    private IUserDigitalAccessRepository userDigitalAccessRepository;

    @Mock
    private IDigitalAssetRepository digitalAssetRepository;

    @Mock
    private IReadingProgressRepository readingProgressRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IFileStorage fileStorage;

    private DigitalLibraryService digitalLibraryService;

    @BeforeEach
    void setUp() {
        FileStorageProperties fileStorageProperties = new FileStorageProperties(
                "r2",
                "private-bucket",
                "auto",
                "https://storage.example.com",
                "key",
                "secret",
                null,
                10L,
                5L,
                5L,
                200L,
                8L * 1024L * 1024L * 1024L,
                500_000L
        );
        digitalLibraryService = new DigitalLibraryService(
                userDigitalAccessRepository,
                digitalAssetRepository,
                readingProgressRepository,
                bookRepository,
                fileStorage,
                fileStorageProperties
        );
    }

    @Test
    void grantPurchasedAccessForOrder_createsPurchasedAccess() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID digitalAssetId = UUID.randomUUID();
        Order order = order(
                userId,
                bookId,
                digitalAssetId,
                PaymentMethod.BANK_TRANSFER_QR,
                PaymentStatus.PAID
        );
        DigitalAsset digitalAsset = digitalAsset(bookId, digitalAssetId, true, true, true);

        when(digitalAssetRepository.findAllByIdsActive(List.of(digitalAssetId))).thenReturn(List.of(digitalAsset));
        when(userDigitalAccessRepository.findLatestByUserIdAndDigitalAssetIdAndAccessType(
                userId,
                digitalAssetId,
                DigitalAccessType.PURCHASED
        )).thenReturn(Optional.empty());
        when(userDigitalAccessRepository.save(org.mockito.ArgumentMatchers.any(UserDigitalAccess.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        digitalLibraryService.grantPurchasedAccessForOrder(order);

        ArgumentCaptor<UserDigitalAccess> captor = ArgumentCaptor.forClass(UserDigitalAccess.class);
        verify(userDigitalAccessRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
        assertEquals(digitalAssetId, captor.getValue().getDigitalAssetId());
        assertEquals(order.getId(), captor.getValue().getSourceOrderId());
        assertEquals(DigitalAccessType.PURCHASED, captor.getValue().getAccessType());
        assertEquals(DigitalAccessStatus.ACTIVE, captor.getValue().getStatus());
    }

    @Test
    void grantPurchasedAccessForOrder_whenActiveAccessAlreadyMatchesOrder_skipsSave() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID digitalAssetId = UUID.randomUUID();
        Order order = order(
                userId,
                bookId,
                digitalAssetId,
                PaymentMethod.BANK_TRANSFER_QR,
                PaymentStatus.PAID
        );
        DigitalAsset digitalAsset = digitalAsset(bookId, digitalAssetId, true, true, true);
        UserDigitalAccess currentAccess = activePurchasedAccess(userId, digitalAssetId, order.getId());

        when(digitalAssetRepository.findAllByIdsActive(List.of(digitalAssetId))).thenReturn(List.of(digitalAsset));
        when(userDigitalAccessRepository.findLatestByUserIdAndDigitalAssetIdAndAccessType(
                userId,
                digitalAssetId,
                DigitalAccessType.PURCHASED
        )).thenReturn(Optional.of(currentAccess));

        digitalLibraryService.grantPurchasedAccessForOrder(order);

        verify(userDigitalAccessRepository, never()).save(org.mockito.ArgumentMatchers.any(UserDigitalAccess.class));
    }

    @Test
    void getMyReadUrl_whenUserHasAccess_returnsSignedReadUrl() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID digitalAssetId = UUID.randomUUID();
        UserDigitalAccess access = activePurchasedAccess(userId, digitalAssetId, UUID.randomUUID());
        DigitalAsset asset = digitalAsset(bookId, digitalAssetId, true, true, true);
        SignedUrlResult signedUrl = new SignedUrlResult(
                "https://signed.example.com/read",
                Instant.parse("2026-06-25T00:05:00Z")
        );

        when(userDigitalAccessRepository.findAllByUserIdAndDigitalAssetIdActive(userId, digitalAssetId))
                .thenReturn(List.of(access));
        when(digitalAssetRepository.findByIdActive(digitalAssetId)).thenReturn(Optional.of(asset));
        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book(bookId)));
        when(readingProgressRepository.findByUserIdAndDigitalAssetId(userId, digitalAssetId)).thenReturn(Optional.empty());
        when(fileStorage.createPresignedDownloadUrl(
                "private-bucket",
                asset.getStorageKey(),
                asset.getFileName(),
                Duration.ofMinutes(5L),
                false
        )).thenReturn(signedUrl);

        SignedUrlResult result = digitalLibraryService.getMyReadUrl(userId, digitalAssetId);

        assertEquals("https://signed.example.com/read", result.url());
    }

    @Test
    void revokePurchasedAccessForOrder_marksPurchasedAccessRevoked() {
        UUID orderId = UUID.randomUUID();
        UserDigitalAccess purchasedAccess = activePurchasedAccess(UUID.randomUUID(), UUID.randomUUID(), orderId);
        UserDigitalAccess rentalAccess = new UserDigitalAccess(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                DigitalAccessType.BORROWED,
                DigitalAccessStatus.ACTIVE,
                orderId,
                Instant.EPOCH.plus(Duration.ofDays(7)),
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );

        when(userDigitalAccessRepository.findAllBySourceOrderIdActive(orderId))
                .thenReturn(List.of(purchasedAccess, rentalAccess));
        when(userDigitalAccessRepository.save(org.mockito.ArgumentMatchers.any(UserDigitalAccess.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        digitalLibraryService.revokePurchasedAccessForOrder(orderId);

        verify(userDigitalAccessRepository).save(purchasedAccess);
        assertEquals(DigitalAccessStatus.REVOKED, purchasedAccess.getStatus());
    }

    @Test
    void updateMyProgress_whenUserHasNoAccess_rejectsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID digitalAssetId = UUID.randomUUID();
        UpdateReadingProgressCommand command = new UpdateReadingProgressCommand(
                userId,
                digitalAssetId,
                1,
                new BigDecimal("10"),
                "{\"page\":1}"
        );

        when(userDigitalAccessRepository.findAllByUserIdAndDigitalAssetIdActive(userId, digitalAssetId))
                .thenReturn(List.of());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> digitalLibraryService.updateMyProgress(command)
        );

        assertEquals(ApplicationErrorCode.DIGITAL_ASSET_NOT_FOUND, exception.getErrorCode());
    }

    private static Order order(
            UUID userId,
            UUID bookId,
            UUID digitalAssetId,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus
    ) {
        Instant now = Instant.EPOCH;
        OrderItem item = new OrderItem(
                UUID.randomUUID(),
                PurchaseItemType.DIGITAL_ASSET,
                bookId,
                digitalAssetId,
                "Book Title",
                new BigDecimal("5.00"),
                1,
                new BigDecimal("5.00")
        );
        return new Order(
                UUID.randomUUID(),
                "DH-DIGITAL-001",
                userId,
                List.of(item),
                new BigDecimal("5.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("5.00"),
                null,
                null,
                null,
                null,
                paymentMethod,
                paymentStatus,
                com.bookstore.bookstore.domain.enums.OrderStatus.DELIVERED,
                "Receiver Name",
                "0900000000",
                "Receiver Address",
                now,
                now,
                null
        );
    }

    private static UserDigitalAccess activePurchasedAccess(UUID userId, UUID digitalAssetId, UUID sourceOrderId) {
        Instant now = Instant.EPOCH;
        return new UserDigitalAccess(
                UUID.randomUUID(),
                userId,
                digitalAssetId,
                DigitalAccessType.PURCHASED,
                DigitalAccessStatus.ACTIVE,
                sourceOrderId,
                null,
                now,
                now,
                null
        );
    }

    private static Book book(UUID bookId) {
        Instant now = Instant.EPOCH;
        return new Book(
                bookId,
                "Book Title",
                "ISBN-123",
                "Book Description",
                new BigDecimal("20.00"),
                5,
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

    private static DigitalAsset digitalAsset(
            UUID bookId,
            UUID digitalAssetId,
            boolean downloadAllowed,
            boolean purchaseAllowed,
            boolean published
    ) {
        Instant now = Instant.EPOCH;
        return new DigitalAsset(
                digitalAssetId,
                bookId,
                DigitalAssetFormat.PDF,
                "Bản PDF",
                fileAsset(FilePurpose.EBOOK_FILE, "ebook.pdf", "private/digital/ebook.pdf", FileVisibility.PRIVATE, FileStatus.ACTIVE),
                null,
                new BigDecimal("5.00"),
                downloadAllowed,
                purchaseAllowed,
                published,
                now,
                now,
                null
        );
    }

    private static FileAsset fileAsset(
            FilePurpose purpose,
            String originalName,
            String storageKey,
            FileVisibility visibility,
            FileStatus status
    ) {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                UUID.randomUUID(),
                FileProvider.R2,
                purpose,
                "private-bucket",
                storageKey,
                null,
                originalName,
                "application/pdf",
                1_024L,
                "checksum",
                visibility,
                status,
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }
}
