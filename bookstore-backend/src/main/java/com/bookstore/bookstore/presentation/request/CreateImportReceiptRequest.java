package com.bookstore.bookstore.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateImportReceiptRequest(
        @NotNull(message = "supplierId khong duoc null")
        UUID supplierId,
        @NotEmpty(message = "items khong duoc de trong")
        List<@Valid CreateImportReceiptItemRequest> items,
        String note
) {
}
