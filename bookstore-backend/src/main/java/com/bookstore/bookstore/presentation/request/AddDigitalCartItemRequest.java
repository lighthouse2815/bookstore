package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddDigitalCartItemRequest(
        @NotNull(message = "digitalAssetId không được null")
        UUID digitalAssetId
) {
}
