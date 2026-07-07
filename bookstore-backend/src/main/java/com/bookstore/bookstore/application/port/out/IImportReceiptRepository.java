package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.ImportReceipt;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IImportReceiptRepository {

    List<ImportReceipt> findAll();

    PageSliceResult<ImportReceipt> findPage(int page, int size);

    Optional<ImportReceipt> findById(UUID receiptId);

    ImportReceipt save(ImportReceipt importReceipt);
}
