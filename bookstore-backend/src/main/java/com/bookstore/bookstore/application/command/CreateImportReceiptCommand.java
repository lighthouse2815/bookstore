package com.bookstore.bookstore.application.command;

import java.util.List;
import java.util.UUID;

public record CreateImportReceiptCommand(
        UUID supplierId,
        List<CreateImportReceiptItemCommand> items,
        String note,
        UUID createdBy
) {
}
