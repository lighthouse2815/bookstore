package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateDigitalAssetCommand;
import com.bookstore.bookstore.application.command.DeleteDigitalAssetCommand;
import com.bookstore.bookstore.application.command.UpdateDigitalAssetCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IDigitalAssetService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DigitalAssetService implements IDigitalAssetService {

    private final IDigitalAssetRepository digitalAssetRepository;
    private final IBookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DigitalAsset> getPublishedByBookId(UUID bookId) {
        requireActiveBook(bookId);
        return digitalAssetRepository.findAllByBookIdActive(bookId).stream()
                .filter(DigitalAsset::isPublished)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DigitalAsset> getAllByBookIdForAdmin(UUID bookId) {
        requireBookExists(bookId);
        return digitalAssetRepository.findAllByBookIdIncludingDeleted(bookId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DigitalAsset create(CreateDigitalAssetCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        requireActiveBook(command.bookId());
        Instant now = Instant.now();
        DigitalAsset digitalAsset = new DigitalAsset(
                UUID.randomUUID(),
                command.bookId(),
                command.format(),
                StringUtils.trimToNull(command.title()),
                StringUtils.trimToNull(command.fileName()),
                StringUtils.trimToNull(command.storageKey()),
                StringUtils.trimToNull(command.mimeType()),
                command.fileSize(),
                StringUtils.trimToNull(command.checksum()),
                StringUtils.trimToNull(command.sampleStorageKey()),
                command.price(),
                command.downloadAllowed(),
                command.published(),
                now,
                now,
                null
        );
        return digitalAssetRepository.save(digitalAsset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DigitalAsset update(UpdateDigitalAssetCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        DigitalAsset currentAsset = digitalAssetRepository.findByIdActive(command.digitalAssetId())
                .filter(asset -> asset.getBookId().equals(command.bookId()))
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.DIGITAL_ASSET_NOT_FOUND));

        currentAsset.updateAsset(
                command.format(),
                StringUtils.trimToNull(command.title()),
                StringUtils.trimToNull(command.fileName()),
                StringUtils.trimToNull(command.storageKey()),
                StringUtils.trimToNull(command.mimeType()),
                command.fileSize(),
                StringUtils.trimToNull(command.checksum()),
                StringUtils.trimToNull(command.sampleStorageKey()),
                command.price(),
                command.downloadAllowed(),
                command.published()
        );

        return digitalAssetRepository.save(currentAsset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteDigitalAssetCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        DigitalAsset currentAsset = digitalAssetRepository.findByIdActive(command.digitalAssetId())
                .filter(asset -> asset.getBookId().equals(command.bookId()))
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.DIGITAL_ASSET_NOT_FOUND));

        currentAsset.softDelete();
        digitalAssetRepository.save(currentAsset);
    }

    private void requireBookExists(UUID bookId) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }
        if (!bookRepository.existsByIdIncludingDeleted(bookId)) {
            throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
        }
    }

    private void requireActiveBook(UUID bookId) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }
        bookRepository.findByIdActive(bookId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));
    }
}
