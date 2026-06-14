package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateAuthorRequest(
        @NotBlank(message = "name khong duoc de trong")
        String name,
        String biography,
        String avatarUrl,
        @Positive(message = "birthYear phai lon hon 0")
        Integer birthYear,
        @Positive(message = "deathYear phai lon hon 0")
        Integer deathYear
) {
}
