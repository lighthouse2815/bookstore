package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserAddressRequest(
        @NotBlank(message = "receiverName không được để trống")
        String receiverName,
        @NotBlank(message = "receiverPhone không được để trống")
        @Pattern(regexp = "0\\d{9}", message = "receiverPhone phải có đúng 10 chữ số và bắt đầu bằng 0")
        String receiverPhone,
        @NotBlank(message = "receiverAddress không được để trống")
        String receiverAddress
) {
}

