package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateImportReceiptCommand;
import com.bookstore.bookstore.application.result.ImportReceiptResult;
import java.util.List;
import java.util.UUID;

public interface IImportReceiptService {

    List<ImportReceiptResult> getAll();

    ImportReceiptResult getById(UUID receiptId);

    ImportReceiptResult create(CreateImportReceiptCommand command);
}
