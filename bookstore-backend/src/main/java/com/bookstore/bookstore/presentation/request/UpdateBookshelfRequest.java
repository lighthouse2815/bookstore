package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBookshelfRequest(
        @NotBlank(message = "name khong duoc de trong")
        @Size(max = 100, message = "name khong duoc vuot qua 100 ky tu")
        String name
) {
}
