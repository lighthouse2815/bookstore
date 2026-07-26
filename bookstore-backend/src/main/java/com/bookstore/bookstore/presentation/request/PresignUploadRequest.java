package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record PresignUploadRequest(
        @NotNull(message = "purpose không được null")
        FilePurpose purpose,

        @NotNull(message = "visibility không được null")
        FileVisibility visibility,

        @NotBlank(message = "fileName không được để trống")
        String fileName,

        @NotBlank(message = "contentType không được để trống")
        String contentType,

        @NotNull(message = "sizeBytes không được null")
        @Positive(message = "sizeBytes phải lớn hơn 0")
        Long sizeBytes,

        UUID bookId,
        UUID authorId,
        UUID digitalAssetId,
        UUID reviewId,
        UUID orderId
) {
}

