package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SucceedRefundRequest(
        @NotBlank @Size(max = 255) String externalReference,
        @Size(max = 1000) String evidenceUrl,
        @Size(max = 10000) String evidenceMetadata
) { }
