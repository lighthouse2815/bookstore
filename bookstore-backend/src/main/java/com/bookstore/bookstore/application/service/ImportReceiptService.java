package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.ImportReceiptAssembler;
import com.bookstore.bookstore.application.command.CreateImportReceiptCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IImportReceiptService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IImportReceiptRepository;
import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.application.port.out.ISupplierRepository;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.ImportReceiptResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.ImportReceipt;
import com.bookstore.bookstore.domain.model.ImportReceiptItem;
import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImportReceiptService implements IImportReceiptService {

    private final IImportReceiptRepository importReceiptRepository;
    private final ISupplierRepository supplierRepository;
    private final IBookRepository bookRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final ImportReceiptAssembler importReceiptAssembler;

    @Override
    @Transactional(readOnly = true)
    public List<ImportReceiptResult> getAll() {
        return importReceiptRepository.findAll().stream()
                .map(importReceiptAssembler::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<ImportReceiptResult> getAll(PageQuery pageQuery) {
        int page = pageQuery.page();
        int size = pageQuery.size();
        return importReceiptRepository.findPage(page, size).map(importReceiptAssembler::toResult);
    }

    @Override
    @Transactional(readOnly = true)
    public ImportReceiptResult getById(UUID receiptId) {
        if (receiptId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "receiptId");
        }

        return importReceiptRepository.findById(receiptId)
                .map(importReceiptAssembler::toResult)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.IMPORT_RECEIPT_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportReceiptResult create(CreateImportReceiptCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        requireActiveSupplier(command.supplierId());

        Map<UUID, Book> booksById = loadImportBooks(command);
        UUID receiptId = UUID.randomUUID();
        Instant now = Instant.now();
        List<ImportReceiptItem> items = new ArrayList<>();
        List<StockMovement> stockMovements = new ArrayList<>();

        for (var commandItem : command.items()) {
            Book book = booksById.get(commandItem.bookId());
            int beforeQuantity = book.getStockQuantity();
            book.increaseStock(commandItem.quantity());
            int afterQuantity = book.getStockQuantity();

            BigDecimal lineTotal = commandItem.unitCost().multiply(BigDecimal.valueOf(commandItem.quantity()));
            items.add(new ImportReceiptItem(
                    UUID.randomUUID(),
                    book.getId(),
                    book.getTitle(),
                    commandItem.unitCost(),
                    commandItem.quantity(),
                    lineTotal
            ));
            stockMovements.add(new StockMovement(
                    UUID.randomUUID(),
                    book.getId(),
                    StockMovementType.IMPORT,
                    commandItem.quantity(),
                    beforeQuantity,
                    afterQuantity,
                    receiptId,
                    "IMPORT_RECEIPT",
                    null,
                    now,
                    command.createdBy()
            ));
        }

        BigDecimal totalAmount = items.stream()
                .map(ImportReceiptItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ImportReceipt importReceipt = new ImportReceipt(
                receiptId,
                command.supplierId(),
                items,
                totalAmount,
                StringUtils.trimToNull(command.note()),
                now,
                now,
                command.createdBy()
        );

        ImportReceipt savedImportReceipt = importReceiptRepository.save(importReceipt);
        stockMovements.forEach(stockMovementRepository::save);
        booksById.values().forEach(bookRepository::save);
        return importReceiptAssembler.toResult(savedImportReceipt);
    }

    private Map<UUID, Book> loadImportBooks(CreateImportReceiptCommand command) {
        Map<UUID, Book> booksById = bookRepository.findAllByIdsIncludingDeletedForUpdate(
                        command.items().stream()
                                .map(item -> item.bookId())
                                .toList()
                ).stream()
                .collect(
                        LinkedHashMap::new,
                        (map, book) -> map.put(book.getId(), book),
                        Map::putAll
                );

        for (var item : command.items()) {
            Book book = booksById.get(item.bookId());
            if (book == null || book.getDeletedAt() != null) {
                throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
            }
        }

        return booksById;
    }

    private void requireActiveSupplier(UUID supplierId) {
        if (!supplierRepository.existsByIdIncludingDeleted(supplierId)) {
            throw new ApplicationException(ApplicationErrorCode.SUPPLIER_NOT_FOUND);
        }
    }

}
