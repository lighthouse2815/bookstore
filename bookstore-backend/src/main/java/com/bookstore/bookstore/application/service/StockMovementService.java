package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.StockMovementAssembler;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IStockMovementService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.application.result.StockMovementResult;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockMovementService implements IStockMovementService {

    private final IStockMovementRepository stockMovementRepository;
    private final IBookRepository bookRepository;
    private final StockMovementAssembler stockMovementAssembler;

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResult> getAll() {
        return stockMovementRepository.findAll().stream()
                .map(stockMovementAssembler::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResult> getByBookId(UUID bookId) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }

        requireBookExists(bookId);

        return stockMovementRepository.findAllByBookId(bookId).stream()
                .map(stockMovementAssembler::toResult)
                .toList();
    }

    private void requireBookExists(UUID bookId) {
        if (!bookRepository.existsByIdIncludingDeleted(bookId)) {
            throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
        }
    }
}
