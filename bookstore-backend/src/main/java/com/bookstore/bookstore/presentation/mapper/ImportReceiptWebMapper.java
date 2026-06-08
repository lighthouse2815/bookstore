package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateImportReceiptCommand;
import com.bookstore.bookstore.application.command.CreateImportReceiptItemCommand;
import com.bookstore.bookstore.application.result.ImportReceiptItemResult;
import com.bookstore.bookstore.application.result.ImportReceiptResult;
import com.bookstore.bookstore.presentation.request.CreateImportReceiptRequest;
import com.bookstore.bookstore.presentation.response.ImportReceiptItemResponse;
import com.bookstore.bookstore.presentation.response.ImportReceiptResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ImportReceiptWebMapper {

    public CreateImportReceiptCommand toCreateCommand(UUID createdBy, CreateImportReceiptRequest request) {
        return new CreateImportReceiptCommand(
                request.supplierId(),
                request.items().stream()
                        .map(item -> new CreateImportReceiptItemCommand(
                                item.bookId(),
                                item.unitCost(),
                                item.quantity()
                        ))
                        .toList(),
                request.note(),
                createdBy
        );
    }

    public ImportReceiptResponse toResponse(ImportReceiptResult result) {
        return new ImportReceiptResponse(
                result.id(),
                result.supplierId(),
                result.items().stream()
                        .map(this::toItemResponse)
                        .toList(),
                result.totalAmount(),
                result.note(),
                result.createdAt(),
                result.updatedAt(),
                result.createdBy()
        );
    }

    private ImportReceiptItemResponse toItemResponse(ImportReceiptItemResult result) {
        return new ImportReceiptItemResponse(
                result.id(),
                result.bookId(),
                result.bookTitle(),
                result.unitCost(),
                result.quantity(),
                result.lineTotal()
        );
    }
}
