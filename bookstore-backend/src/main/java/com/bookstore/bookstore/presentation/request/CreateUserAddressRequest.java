package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserAddressRequest(
        @NotBlank(message = "receiverName khong duoc de trong")
        String receiverName,
        @NotBlank(message = "receiverPhone khong duoc de trong")
        @Pattern(regexp = "0\\d{9}", message = "receiverPhone phai co dung 10 chu so va bat dau bang 0")
        String receiverPhone,
        @NotBlank(message = "receiverAddress khong duoc de trong")
        String receiverAddress
) {
}
