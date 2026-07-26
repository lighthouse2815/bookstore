package com.bookstore.bookstore.presentation.request;

import jakarta.validation.constraints.Size;

public record ReviewModerationRequest(
        @Size(max = 500, message = "ly do kiem duyet khong duoc vuot qua 500 ky tu")
        String reason
) {
}
