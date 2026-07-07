package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CompleteFileUploadCommand;
import com.bookstore.bookstore.application.command.PresignUploadCommand;
import com.bookstore.bookstore.application.result.PresignedUploadResult;
import com.bookstore.bookstore.application.result.SignedUrlResult;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.presentation.request.CompleteFileUploadRequest;
import com.bookstore.bookstore.presentation.request.PresignUploadRequest;
import com.bookstore.bookstore.presentation.response.FileAssetResponse;
import com.bookstore.bookstore.presentation.response.PresignedUploadResponse;
import com.bookstore.bookstore.presentation.response.SignedUrlResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FileWebMapper {

    public PresignUploadCommand toPresignUploadCommand(
            UUID requesterId,
            boolean admin,
            PresignUploadRequest request
    ) {
        return new PresignUploadCommand(
                requesterId,
                admin,
                request.purpose(),
                request.visibility(),
                request.fileName(),
                request.contentType(),
                request.sizeBytes(),
                request.bookId(),
                request.authorId(),
                request.digitalAssetId(),
                request.reviewId(),
                request.orderId()
        );
    }

    public CompleteFileUploadCommand toCompleteUploadCommand(
            UUID requesterId,
            boolean admin,
            CompleteFileUploadRequest request
    ) {
        return new CompleteFileUploadCommand(
                requesterId,
                admin,
                request.fileAssetId(),
                request.checksumSha256()
        );
    }

    public PresignedUploadResponse toPresignedUploadResponse(PresignedUploadResult result) {
        return new PresignedUploadResponse(
                result.fileAssetId(),
                result.uploadUrl(),
                result.method(),
                result.headers(),
                result.expiresAt(),
                result.storageKey()
        );
    }

    public FileAssetResponse toFileAssetResponse(FileAsset fileAsset) {
        return new FileAssetResponse(
                fileAsset.getId(),
                fileAsset.getProvider(),
                fileAsset.getPurpose(),
                fileAsset.getBucket(),
                fileAsset.getStorageKey(),
                fileAsset.getPublicUrl(),
                fileAsset.getOriginalName(),
                fileAsset.getContentType(),
                fileAsset.getSizeBytes(),
                fileAsset.getChecksumSha256(),
                fileAsset.getVisibility(),
                fileAsset.getStatus(),
                fileAsset.getCreatedBy(),
                fileAsset.getCreatedAt(),
                fileAsset.getUpdatedAt(),
                fileAsset.getDeletedAt()
        );
    }

    public SignedUrlResponse toSignedUrlResponse(SignedUrlResult result) {
        return new SignedUrlResponse(result.url(), result.expiresAt());
    }
}
