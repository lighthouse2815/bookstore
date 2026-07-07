package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.UpdateReadingProgressCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IDigitalLibraryService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.application.port.out.IFileStorage;
import com.bookstore.bookstore.application.port.out.IReadingProgressRepository;
import com.bookstore.bookstore.application.port.out.IUserDigitalAccessRepository;
import com.bookstore.bookstore.application.result.DigitalLibraryAssetResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.SignedUrlResult;
import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.enums.DigitalAccessType;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.ReadingProgress;
import com.bookstore.bookstore.domain.model.UserDigitalAccess;
import com.bookstore.bookstore.infrastructure.storage.FileStorageProperties;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DigitalLibraryService implements IDigitalLibraryService {

    private final IUserDigitalAccessRepository userDigitalAccessRepository;
    private final IDigitalAssetRepository digitalAssetRepository;
    private final IReadingProgressRepository readingProgressRepository;
    private final IBookRepository bookRepository;
    private final IFileStorage fileStorage;
    private final FileStorageProperties fileStorageProperties;

    @Override
    @Transactional(readOnly = true)
    public List<DigitalLibraryAssetResult> getMyLibrary(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        List<UserDigitalAccess> activeAccesses = userDigitalAccessRepository.findAllByUserIdActive(userId).stream()
                .filter(this::isAccessibleAccess)
                .toList();
        return toLibraryResults(userId, activeAccesses);
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<DigitalLibraryAssetResult> getMyLibrary(UUID userId, int page, int size) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        validatePageRequest(page, size);
        PageSliceResult<UserDigitalAccess> accessPage = userDigitalAccessRepository.findAccessiblePageByUserId(
                userId,
                Instant.now(),
                page,
                size
        );
        return new PageSliceResult<>(
                toLibraryResults(userId, accessPage.items()),
                accessPage.totalCount(),
                accessPage.page(),
                accessPage.size()
        );
    }

    private List<DigitalLibraryAssetResult> toLibraryResults(UUID userId, List<UserDigitalAccess> activeAccesses) {
        if (activeAccesses.isEmpty()) {
            return List.of();
        }

        List<UUID> digitalAssetIds = activeAccesses.stream()
                .map(UserDigitalAccess::getDigitalAssetId)
                .distinct()
                .toList();
        Map<UUID, DigitalAsset> digitalAssetsById = digitalAssetRepository.findAllByIdsActive(digitalAssetIds).stream()
                .collect(
                        LinkedHashMap::new,
                        (map, asset) -> map.put(asset.getId(), asset),
                        Map::putAll
                );
        Map<UUID, Book> booksById = bookRepository.findAllByIdsIncludingDeleted(
                        digitalAssetsById.values().stream()
                                .map(DigitalAsset::getBookId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(
                        LinkedHashMap::new,
                        (map, book) -> map.put(book.getId(), book),
                        Map::putAll
                );
        Map<UUID, ReadingProgress> progressByAssetId = readingProgressRepository.findAllByUserId(userId).stream()
                .collect(
                        LinkedHashMap::new,
                        (map, progress) -> map.put(progress.getDigitalAssetId(), progress),
                        Map::putAll
                );

        return activeAccesses.stream()
                .map(access -> toLibraryResult(
                        access,
                        digitalAssetsById.get(access.getDigitalAssetId()),
                        booksById,
                        progressByAssetId
                ))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DigitalLibraryAssetResult getMyAsset(UUID userId, UUID digitalAssetId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
        if (digitalAssetId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "digitalAssetId");
        }

        UserDigitalAccess access = loadAccessibleAccess(userId, digitalAssetId);
        DigitalAsset digitalAsset = digitalAssetRepository.findByIdActive(digitalAssetId)
                .filter(DigitalAsset::isPublished)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.DIGITAL_ASSET_NOT_FOUND));
        Book book = bookRepository.findByIdActive(digitalAsset.getBookId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));
        ReadingProgress progress = readingProgressRepository.findByUserIdAndDigitalAssetId(userId, digitalAssetId)
                .orElse(null);

        return new DigitalLibraryAssetResult(access, digitalAsset, book, progress);
    }

    @Override
    @Transactional(readOnly = true)
    public SignedUrlResult getPublishedSampleUrl(UUID bookId, UUID digitalAssetId) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }
        if (digitalAssetId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "digitalAssetId");
        }

        DigitalAsset asset = digitalAssetRepository.findByIdActive(digitalAssetId)
                .filter(currentAsset -> currentAsset.getBookId().equals(bookId))
                .filter(DigitalAsset::isPublished)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.DIGITAL_ASSET_NOT_FOUND));

        requireStorageConfigured();
        FileAsset sampleFileAsset = requirePrivateFileAsset(asset.getSampleFileAsset(), FilePurpose.SAMPLE_FILE);
        return fileStorage.createPresignedDownloadUrl(
                resolveBucket(sampleFileAsset),
                sampleFileAsset.getStorageKey(),
                sampleFileAsset.getOriginalName(),
                Duration.ofMinutes(fileStorageProperties.resolvedPresignDownloadExpireMinutes()),
                false
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SignedUrlResult getMyReadUrl(UUID userId, UUID digitalAssetId) {
        DigitalAsset asset = getMyAsset(userId, digitalAssetId).asset();
        requireStorageConfigured();
        FileAsset fileAsset = requirePrivateFileAsset(asset.getFileAsset(), FilePurpose.EBOOK_FILE);
        return fileStorage.createPresignedDownloadUrl(
                resolveBucket(fileAsset),
                fileAsset.getStorageKey(),
                fileAsset.getOriginalName(),
                Duration.ofMinutes(fileStorageProperties.resolvedPresignDownloadExpireMinutes()),
                false
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SignedUrlResult getMyDownloadUrl(UUID userId, UUID digitalAssetId) {
        DigitalLibraryAssetResult result = getMyAsset(userId, digitalAssetId);
        if (!result.asset().isDownloadAllowed()) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_DOWNLOAD_NOT_ALLOWED);
        }

        requireStorageConfigured();
        FileAsset fileAsset = requirePrivateFileAsset(result.asset().getFileAsset(), FilePurpose.EBOOK_FILE);
        return fileStorage.createPresignedDownloadUrl(
                resolveBucket(fileAsset),
                fileAsset.getStorageKey(),
                fileAsset.getOriginalName(),
                Duration.ofMinutes(fileStorageProperties.resolvedPresignDownloadExpireMinutes()),
                true
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReadingProgress updateMyProgress(UpdateReadingProgressCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        loadAccessibleAccess(command.userId(), command.digitalAssetId());
        Instant now = Instant.now();
        ReadingProgress currentProgress = readingProgressRepository
                .findByUserIdAndDigitalAssetId(command.userId(), command.digitalAssetId())
                .orElse(null);
        if (currentProgress == null) {
            return readingProgressRepository.save(new ReadingProgress(
                    UUID.randomUUID(),
                    command.userId(),
                    command.digitalAssetId(),
                    command.currentPage(),
                    command.progressPercent(),
                    StringUtils.trimToNull(command.positionData()),
                    now,
                    now,
                    now
            ));
        }

        currentProgress.updateProgress(
                command.currentPage(),
                command.progressPercent(),
                StringUtils.trimToNull(command.positionData()),
                now
        );
        return readingProgressRepository.save(currentProgress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantPurchasedAccessForOrder(Order order) {
        if (order == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "order");
        }

        List<UUID> digitalAssetIds = order.getItems().stream()
                .filter(item -> item.getItemType() == com.bookstore.bookstore.domain.enums.PurchaseItemType.DIGITAL_ASSET)
                .map(item -> item.getDigitalAssetId())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (digitalAssetIds.isEmpty()) {
            return;
        }

        List<DigitalAsset> digitalAssets = digitalAssetRepository.findAllByIdsActive(digitalAssetIds).stream()
                .filter(DigitalAsset::isPublished)
                .filter(DigitalAsset::isPurchaseAllowed)
                .toList();
        if (digitalAssets.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        for (DigitalAsset digitalAsset : digitalAssets) {
            UserDigitalAccess currentAccess = userDigitalAccessRepository.findLatestByUserIdAndDigitalAssetIdAndAccessType(
                    order.getUserId(),
                    digitalAsset.getId(),
                    DigitalAccessType.PURCHASED
            ).orElse(null);

            if (currentAccess == null) {
                userDigitalAccessRepository.save(new UserDigitalAccess(
                        UUID.randomUUID(),
                        order.getUserId(),
                        digitalAsset.getId(),
                        DigitalAccessType.PURCHASED,
                        DigitalAccessStatus.ACTIVE,
                        order.getId(),
                        null,
                        now,
                        now,
                        null
                ));
                continue;
            }

            if (currentAccess.getStatus() == DigitalAccessStatus.ACTIVE
                    && Objects.equals(currentAccess.getSourceOrderId(), order.getId())
                    && currentAccess.getExpiresAt() == null) {
                continue;
            }

            currentAccess.grant(order.getId(), null, now);
            userDigitalAccessRepository.save(currentAccess);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokePurchasedAccessForOrder(UUID orderId) {
        if (orderId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "orderId");
        }

        Instant now = Instant.now();
        userDigitalAccessRepository.findAllBySourceOrderIdActive(orderId).stream()
                .filter(access -> access.getAccessType() == DigitalAccessType.PURCHASED)
                .filter(access -> access.getStatus() == DigitalAccessStatus.ACTIVE)
                .forEach(access -> {
                    access.revoke(now);
                    userDigitalAccessRepository.save(access);
                });
    }

    private UserDigitalAccess loadAccessibleAccess(UUID userId, UUID digitalAssetId) {
        return userDigitalAccessRepository.findAllByUserIdAndDigitalAssetIdActive(userId, digitalAssetId).stream()
                .filter(this::isAccessibleAccess)
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.DIGITAL_ASSET_NOT_FOUND));
    }

    private boolean isAccessibleAccess(UserDigitalAccess access) {
        if (access.getStatus() != DigitalAccessStatus.ACTIVE) {
            return false;
        }
        Instant expiresAt = access.getExpiresAt();
        return expiresAt == null || expiresAt.isAfter(Instant.now());
    }

    private DigitalLibraryAssetResult toLibraryResult(
            UserDigitalAccess access,
            DigitalAsset digitalAsset,
            Map<UUID, Book> booksById,
            Map<UUID, ReadingProgress> progressByAssetId
    ) {
        if (digitalAsset == null || !digitalAsset.isPublished()) {
            return null;
        }
        Book book = booksById.get(digitalAsset.getBookId());
        if (book == null) {
            return null;
        }
        return new DigitalLibraryAssetResult(
                access,
                digitalAsset,
                book,
                progressByAssetId.get(digitalAsset.getId())
        );
    }

    private void requireStorageConfigured() {
        if (!fileStorageProperties.isConfigured()) {
            throw new ApplicationException(ApplicationErrorCode.FILE_STORAGE_NOT_CONFIGURED);
        }
    }

    private FileAsset requirePrivateFileAsset(FileAsset fileAsset, FilePurpose purpose) {
        if (fileAsset == null) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_NOT_FOUND);
        }
        if (fileAsset.getStatus() == FileStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_UPLOAD_NOT_COMPLETED);
        }
        if (!fileAsset.isActive()) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_NOT_FOUND);
        }
        if (fileAsset.getVisibility() != FileVisibility.PRIVATE) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_INVALID_VISIBILITY);
        }
        if (fileAsset.getPurpose() != purpose) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_INVALID_PURPOSE);
        }
        return fileAsset;
    }

    private String resolveBucket(FileAsset fileAsset) {
        if (fileAsset.getBucket() != null && !fileAsset.getBucket().isBlank()) {
            return fileAsset.getBucket();
        }
        return fileStorageProperties.bucket();
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page");
        }

        if (size <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "size");
        }
    }
}
