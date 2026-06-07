package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record UpdatePublisherRequest(
        @NotBlank(message = "name khong duoc de trong")
        String name,
        String description
) {
}
