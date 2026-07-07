package com.bookstore.bookstore.presentation.request;

import com.bookstore.bookstore.domain.enums.DigitalAssetFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateDigitalAssetRequest(
        @NotNull(message = "format không được null")
        DigitalAssetFormat format,

        @NotBlank(message = "title không được để trống")
        String title,

        @NotNull(message = "fileAssetId không được null")
        UUID fileAssetId,

        UUID sampleFileAssetId,

        @NotNull(message = "price không được null")
        @DecimalMin(value = "0.0", message = "price không được âm")
        BigDecimal price,

        @NotNull(message = "downloadAllowed không được null")
        Boolean downloadAllowed,

        @NotNull(message = "purchaseAllowed không được null")
        Boolean purchaseAllowed,

        @NotNull(message = "published không được null")
        Boolean published
) {
}
