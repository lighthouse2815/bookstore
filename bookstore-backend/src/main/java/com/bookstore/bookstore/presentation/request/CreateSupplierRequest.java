package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSupplierRequest(
        @NotBlank(message = "name không được để trống")
        String name,
        String phone,
        String email,
        String address,
        String note
) {
}

