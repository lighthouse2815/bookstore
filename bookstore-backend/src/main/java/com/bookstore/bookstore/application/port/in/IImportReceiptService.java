package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateImportReceiptCommand;
import com.bookstore.bookstore.application.result.ImportReceiptResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.UUID;

public interface IImportReceiptService {

    List<ImportReceiptResult> getAll();

    PageSliceResult<ImportReceiptResult> getAll(int page, int size);

    ImportReceiptResult getById(UUID receiptId);

    ImportReceiptResult create(CreateImportReceiptCommand command);
}
