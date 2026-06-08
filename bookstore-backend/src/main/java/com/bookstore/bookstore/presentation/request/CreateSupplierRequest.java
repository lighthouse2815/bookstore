package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSupplierRequest(
        @NotBlank(message = "name khong duoc de trong")
        String name,
        String phone,
        String email,
        String address,
        String note
) {
}
