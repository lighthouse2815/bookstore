package com.bookstore.bookstore.application.assembler;

import com.bookstore.bookstore.application.result.ImportReceiptItemResult;
import com.bookstore.bookstore.application.result.ImportReceiptResult;
import com.bookstore.bookstore.domain.model.ImportReceipt;
import com.bookstore.bookstore.domain.model.ImportReceiptItem;
import org.springframework.stereotype.Component;

@Component
public class ImportReceiptAssembler {

    public ImportReceiptResult toResult(ImportReceipt importReceipt) {
        return new ImportReceiptResult(
                importReceipt.getId(),
                importReceipt.getSupplierId(),
                importReceipt.getItems().stream()
                        .map(this::toItemResult)
                        .toList(),
                importReceipt.getTotalAmount(),
                importReceipt.getNote(),
                importReceipt.getCreatedAt(),
                importReceipt.getUpdatedAt(),
                importReceipt.getCreatedBy()
        );
    }

    private ImportReceiptItemResult toItemResult(ImportReceiptItem item) {
        return new ImportReceiptItemResult(
                item.getId(),
                item.getBookId(),
                item.getBookTitle(),
                item.getUnitCost(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }
}
