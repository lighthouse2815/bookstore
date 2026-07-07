package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IFileAssetRepository;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.FileAsset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileAssetPolicyService {

    private final IFileAssetRepository fileAssetRepository;

    public FileAsset requireActiveAsset(UUID fileAssetId, FilePurpose purpose, FileVisibility visibility) {
        if (fileAssetId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "fileAssetId");
        }

        FileAsset fileAsset = fileAssetRepository.findByIdIncludingDeleted(fileAssetId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.FILE_ASSET_NOT_FOUND));

        if (fileAsset.getStatus() == FileStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_UPLOAD_NOT_COMPLETED);
        }

        if (!fileAsset.isActive()) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_NOT_FOUND);
        }

        if (fileAsset.getPurpose() != purpose) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_INVALID_PURPOSE);
        }

        if (fileAsset.getVisibility() != visibility) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_INVALID_VISIBILITY);
        }

        return fileAsset;
    }

    public FileAsset requireActiveOwnedAsset(
            UUID fileAssetId,
            FilePurpose purpose,
            FileVisibility visibility,
            UUID ownerId
    ) {
        if (ownerId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "ownerId");
        }

        FileAsset fileAsset = requireActiveAsset(fileAssetId, purpose, visibility);
        if (!ownerId.equals(fileAsset.getCreatedBy())) {
            throw new ApplicationException(ApplicationErrorCode.FILE_ASSET_ACCESS_DENIED);
        }

        return fileAsset;
    }
}
