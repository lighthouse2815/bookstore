package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CompleteFileUploadCommand;
import com.bookstore.bookstore.application.command.PresignUploadCommand;
import com.bookstore.bookstore.application.result.PresignedUploadResult;
import com.bookstore.bookstore.domain.model.FileAsset;
import java.util.UUID;

public interface IFileAssetService {

    PresignedUploadResult createPresignedUpload(PresignUploadCommand command);

    FileAsset completeUpload(CompleteFileUploadCommand command);

    FileAsset getById(UUID fileAssetId, UUID requesterId, boolean admin);

    void delete(UUID fileAssetId, UUID requesterId, boolean admin);
}
