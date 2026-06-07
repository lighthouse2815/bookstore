package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePublisherRequest(
        @NotBlank(message = "name khong duoc de trong")
        String name,
        String description
) {
}
