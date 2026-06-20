package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateDigitalAssetRequest(
        @NotNull(message = "format khong duoc null")
        DigitalAssetFormat format,

        @NotBlank(message = "title khong duoc de trong")
        String title,

        @NotBlank(message = "fileName khong duoc de trong")
        String fileName,

        @NotBlank(message = "storageKey khong duoc de trong")
        String storageKey,

        @NotBlank(message = "mimeType khong duoc de trong")
        String mimeType,

        @NotNull(message = "fileSize khong duoc null")
        @PositiveOrZero(message = "fileSize khong duoc am")
        Long fileSize,

        String checksum,

        String sampleStorageKey,

        @NotNull(message = "price khong duoc null")
        @DecimalMin(value = "0.0", message = "price khong duoc am")
        BigDecimal price,

        @NotNull(message = "downloadAllowed khong duoc null")
        Boolean downloadAllowed,

        @NotNull(message = "published khong duoc null")
        Boolean published
) {
}
