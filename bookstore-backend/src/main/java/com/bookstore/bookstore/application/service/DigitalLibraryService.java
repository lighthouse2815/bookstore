package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.UpdateReadingProgressCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IDigitalLibraryService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.application.port.out.IReadingProgressRepository;
import com.bookstore.bookstore.application.port.out.IUserDigitalAccessRepository;
import com.bookstore.bookstore.application.result.DigitalLibraryAssetResult;
import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.enums.DigitalAccessType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.ReadingProgress;
import com.bookstore.bookstore.domain.model.UserDigitalAccess;
import com.bookstore.bookstore.shared.util.StringUtils;
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

    @Override
    @Transactional(readOnly = true)
    public List<DigitalLibraryAssetResult> getMyLibrary(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        List<UserDigitalAccess> activeAccesses = userDigitalAccessRepository.findAllByUserIdActive(userId).stream()
                .filter(this::isAccessibleAccess)
                .toList();
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
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.DIGITAL_ASSET_NOT_FOUND));
        Book book = bookRepository.findByIdActive(digitalAsset.getBookId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));
        ReadingProgress progress = readingProgressRepository.findByUserIdAndDigitalAssetId(userId, digitalAssetId)
                .orElse(null);

        return new DigitalLibraryAssetResult(access, digitalAsset, book, progress);
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

        List<UUID> bookIds = order.getItems().stream()
                .map(item -> item.getBookId())
                .distinct()
                .toList();
        if (bookIds.isEmpty()) {
            return;
        }

        List<DigitalAsset> digitalAssets = digitalAssetRepository.findAllByBookIdsActive(bookIds);
        if (digitalAssets.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        for (DigitalAsset digitalAsset : digitalAssets) {
            UserDigitalAccess currentAccess = userDigitalAccessRepository
                    .findAllByUserIdAndDigitalAssetIdActive(order.getUserId(), digitalAsset.getId())
                    .stream()
                    .filter(access -> access.getAccessType() == DigitalAccessType.PURCHASED)
                    .findFirst()
                    .orElse(null);

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
        if (digitalAsset == null) {
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
}
