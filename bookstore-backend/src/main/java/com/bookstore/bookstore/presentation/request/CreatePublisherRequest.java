package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePublisherRequest(
        @NotBlank(message = "name không được để trống")
        String name,
        String description
) {
}

