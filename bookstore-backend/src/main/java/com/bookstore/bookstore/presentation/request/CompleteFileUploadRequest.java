package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CompleteFileUploadRequest(
        @NotNull(message = "fileAssetId không được null")
        UUID fileAssetId,
        String checksumSha256
) {
}

