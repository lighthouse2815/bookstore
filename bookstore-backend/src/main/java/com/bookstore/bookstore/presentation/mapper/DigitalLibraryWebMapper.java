package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateDigitalAssetCommand;
import com.bookstore.bookstore.application.command.DeleteDigitalAssetCommand;
import com.bookstore.bookstore.application.command.UpdateDigitalAssetCommand;
import com.bookstore.bookstore.application.command.UpdateReadingProgressCommand;
import com.bookstore.bookstore.application.result.DigitalLibraryAssetResult;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.domain.model.ReadingProgress;
import com.bookstore.bookstore.presentation.request.CreateDigitalAssetRequest;
import com.bookstore.bookstore.presentation.request.UpdateDigitalAssetRequest;
import com.bookstore.bookstore.presentation.request.UpdateReadingProgressRequest;
import com.bookstore.bookstore.presentation.response.DigitalAssetResponse;
import com.bookstore.bookstore.presentation.response.DigitalLibraryAssetResponse;
import com.bookstore.bookstore.presentation.response.DigitalLibraryItemResponse;
import com.bookstore.bookstore.presentation.response.PublishedDigitalAssetResponse;
import com.bookstore.bookstore.presentation.response.ReadingProgressResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DigitalLibraryWebMapper {

    public CreateDigitalAssetCommand toCreateDigitalAssetCommand(UUID bookId, CreateDigitalAssetRequest request) {
        return new CreateDigitalAssetCommand(
                bookId,
                request.format(),
                request.title(),
                request.fileName(),
                request.storageKey(),
                request.mimeType(),
                request.fileSize(),
                request.checksum(),
                request.sampleStorageKey(),
                request.price(),
                Boolean.TRUE.equals(request.downloadAllowed()),
                Boolean.TRUE.equals(request.published())
        );
    }

    public UpdateDigitalAssetCommand toUpdateDigitalAssetCommand(
            UUID bookId,
            UUID digitalAssetId,
            UpdateDigitalAssetRequest request
    ) {
        return new UpdateDigitalAssetCommand(
                bookId,
                digitalAssetId,
                request.format(),
                request.title(),
                request.fileName(),
                request.storageKey(),
                request.mimeType(),
                request.fileSize(),
                request.checksum(),
                request.sampleStorageKey(),
                request.price(),
                Boolean.TRUE.equals(request.downloadAllowed()),
                Boolean.TRUE.equals(request.published())
        );
    }

    public DeleteDigitalAssetCommand toDeleteDigitalAssetCommand(UUID bookId, UUID digitalAssetId) {
        return new DeleteDigitalAssetCommand(bookId, digitalAssetId);
    }

    public UpdateReadingProgressCommand toUpdateReadingProgressCommand(
            UUID userId,
            UUID digitalAssetId,
            UpdateReadingProgressRequest request
    ) {
        return new UpdateReadingProgressCommand(
                userId,
                digitalAssetId,
                request.currentPage(),
                request.progressPercent(),
                request.positionData()
        );
    }

    public DigitalAssetResponse toDigitalAssetResponse(DigitalAsset asset) {
        return new DigitalAssetResponse(
                asset.getId(),
                asset.getBookId(),
                asset.getFormat(),
                asset.getTitle(),
                asset.getFileName(),
                asset.getStorageKey(),
                asset.getMimeType(),
                asset.getFileSize(),
                asset.getChecksum(),
                asset.getSampleStorageKey(),
                asset.getPrice(),
                asset.isDownloadAllowed(),
                asset.isPublished(),
                asset.getCreatedAt(),
                asset.getUpdatedAt(),
                asset.getDeletedAt()
        );
    }

    public PublishedDigitalAssetResponse toPublishedDigitalAssetResponse(DigitalAsset asset) {
        return new PublishedDigitalAssetResponse(
                asset.getId(),
                asset.getBookId(),
                asset.getFormat(),
                asset.getTitle(),
                asset.getFileName(),
                asset.getSampleStorageKey(),
                asset.getPrice(),
                asset.isDownloadAllowed()
        );
    }

    public DigitalLibraryItemResponse toDigitalLibraryItemResponse(DigitalLibraryAssetResult result) {
        return new DigitalLibraryItemResponse(
                result.asset().getId(),
                result.book().getId(),
                result.book().getTitle(),
                result.book().getPrimaryImageUrl(),
                result.asset().getTitle(),
                result.asset().getFormat(),
                result.asset().getPrice(),
                result.asset().isDownloadAllowed(),
                result.asset().getSampleStorageKey(),
                result.access().getAccessType(),
                result.access().getStatus(),
                result.access().getSourceOrderId(),
                result.access().getExpiresAt(),
                result.access().getCreatedAt(),
                toReadingProgressResponse(result.progress())
        );
    }

    public DigitalLibraryAssetResponse toDigitalLibraryAssetResponse(DigitalLibraryAssetResult result) {
        return new DigitalLibraryAssetResponse(
                result.asset().getId(),
                result.book().getId(),
                result.book().getTitle(),
                result.book().getDescription(),
                result.book().getPrimaryImageUrl(),
                result.asset().getTitle(),
                result.asset().getFormat(),
                result.asset().getFileName(),
                result.asset().getStorageKey(),
                result.asset().getSampleStorageKey(),
                result.asset().getMimeType(),
                result.asset().getFileSize(),
                result.asset().getChecksum(),
                result.asset().getPrice(),
                result.asset().isDownloadAllowed(),
                result.access().getAccessType(),
                result.access().getStatus(),
                result.access().getSourceOrderId(),
                result.access().getExpiresAt(),
                result.access().getCreatedAt(),
                result.asset().getUpdatedAt(),
                toReadingProgressResponse(result.progress())
        );
    }

    public ReadingProgressResponse toReadingProgressResponse(ReadingProgress progress) {
        if (progress == null) {
            return null;
        }

        return new ReadingProgressResponse(
                progress.getId(),
                progress.getUserId(),
                progress.getDigitalAssetId(),
                progress.getCurrentPage(),
                progress.getProgressPercent(),
                progress.getPositionData(),
                progress.getLastReadAt(),
                progress.getCreatedAt(),
                progress.getUpdatedAt()
        );
    }
}
