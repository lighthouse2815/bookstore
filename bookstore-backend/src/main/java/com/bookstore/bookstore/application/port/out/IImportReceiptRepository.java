package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.ImportReceipt;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IImportReceiptRepository {

    List<ImportReceipt> findAll();

    Optional<ImportReceipt> findById(UUID receiptId);

    ImportReceipt save(ImportReceipt importReceipt);
}
