package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record UpdateAuthorRequest(
        @NotBlank(message = "name không được để trống")
        String name,
        String biography,
        UUID avatarFileAssetId,
        @Positive(message = "birthYear phải lớn hơn 0")
        Integer birthYear,
        @Positive(message = "deathYear phải lớn hơn 0")
        Integer deathYear
) {
}

