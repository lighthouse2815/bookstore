package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.BookshelfAssembler;
import com.bookstore.bookstore.application.command.AddBookshelfItemCommand;
import com.bookstore.bookstore.application.command.CreateBookshelfCommand;
import com.bookstore.bookstore.application.command.DeleteBookshelfCommand;
import com.bookstore.bookstore.application.command.RemoveBookshelfItemCommand;
import com.bookstore.bookstore.application.command.ReorderBookshelfItemsCommand;
import com.bookstore.bookstore.application.command.UpdateBookshelfCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IBookshelfService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IBookshelfRepository;
import com.bookstore.bookstore.application.result.BookshelfResult;
import com.bookstore.bookstore.application.result.BookshelfSummaryResult;
import com.bookstore.bookstore.domain.model.Bookshelf;
import com.bookstore.bookstore.domain.model.BookshelfItem;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookshelfService implements IBookshelfService {

    private final IBookshelfRepository bookshelfRepository;
    private final IBookRepository bookRepository;
    private final BookshelfAssembler bookshelfAssembler;

    @Override
    @Transactional(readOnly = true)
    public List<BookshelfSummaryResult> getMyBookshelves(UUID userId) {
        validateId(userId, "userId");
        List<Bookshelf> bookshelves = bookshelfRepository.findAllByUserIdActive(userId);
        return bookshelfAssembler.toSummaryResults(
                bookshelves,
                bookshelfRepository.countActiveItemsByShelfIds(
                        bookshelves.stream().map(Bookshelf::getId).toList()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BookshelfResult getMyBookshelf(UUID userId, UUID shelfId) {
        validateId(userId, "userId");
        validateId(shelfId, "shelfId");
        return loadBookshelfResult(getOwnedBookshelfOrThrow(shelfId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookshelfResult create(CreateBookshelfCommand command) {
        validateId(command.userId(), "userId");
        String normalizedName = normalizeName(command.name());

        Bookshelf existing = bookshelfRepository.findByUserIdAndName(command.userId(), normalizedName).orElse(null);
        if (existing != null) {
            if (existing.isDeleted()) {
                existing.restore();
                return loadBookshelfResult(bookshelfRepository.save(existing));
            }
            throw new ApplicationException(ApplicationErrorCode.BOOKSHELF_NAME_ALREADY_EXISTS);
        }

        Instant now = Instant.now();
        Bookshelf bookshelf = new Bookshelf(
                UUID.randomUUID(),
                command.userId(),
                normalizedName,
                now,
                now,
                null
        );
        return loadBookshelfResult(bookshelfRepository.save(bookshelf));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookshelfResult update(UpdateBookshelfCommand command) {
        validateId(command.userId(), "userId");
        validateId(command.shelfId(), "shelfId");
        String normalizedName = normalizeName(command.name());

        Bookshelf bookshelf = getOwnedBookshelfOrThrow(command.shelfId(), command.userId());
        if (bookshelf.getName().equals(normalizedName)) {
            return loadBookshelfResult(bookshelf);
        }

        Bookshelf existing = bookshelfRepository.findByUserIdAndName(command.userId(), normalizedName).orElse(null);
        if (existing != null && !existing.getId().equals(bookshelf.getId())) {
            throw new ApplicationException(ApplicationErrorCode.BOOKSHELF_NAME_ALREADY_EXISTS);
        }

        bookshelf.rename(normalizedName);
        return loadBookshelfResult(bookshelfRepository.save(bookshelf));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteBookshelfCommand command) {
        validateId(command.userId(), "userId");
        validateId(command.shelfId(), "shelfId");

        Bookshelf bookshelf = getOwnedBookshelfOrThrow(command.shelfId(), command.userId());
        bookshelf.softDelete();
        bookshelfRepository.save(bookshelf);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookshelfResult addItem(AddBookshelfItemCommand command) {
        validateId(command.userId(), "userId");
        validateId(command.shelfId(), "shelfId");
        validateId(command.bookId(), "bookId");

        Bookshelf bookshelf = getOwnedBookshelfOrThrow(command.shelfId(), command.userId());
        bookRepository.findByIdActive(command.bookId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        BookshelfItem existingItem = bookshelfRepository.findItemByShelfIdAndBookId(command.shelfId(), command.bookId())
                .orElse(null);
        if (existingItem != null) {
            if (existingItem.isDeleted()) {
                existingItem.restore((int) bookshelfRepository.countActiveItemsByShelfId(command.shelfId()));
                bookshelfRepository.saveItem(existingItem);
            }
            return loadBookshelfResult(bookshelf);
        }

        Instant now = Instant.now();
        bookshelfRepository.saveItem(new BookshelfItem(
                UUID.randomUUID(),
                command.shelfId(),
                command.bookId(),
                (int) bookshelfRepository.countActiveItemsByShelfId(command.shelfId()),
                now,
                now,
                null
        ));
        return loadBookshelfResult(bookshelf);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookshelfResult removeItem(RemoveBookshelfItemCommand command) {
        validateId(command.userId(), "userId");
        validateId(command.shelfId(), "shelfId");
        validateId(command.bookId(), "bookId");

        Bookshelf bookshelf = getOwnedBookshelfOrThrow(command.shelfId(), command.userId());
        BookshelfItem item = bookshelfRepository.findItemByShelfIdAndBookId(command.shelfId(), command.bookId())
                .filter(existing -> !existing.isDeleted())
                .orElse(null);
        if (item == null) {
            return loadBookshelfResult(bookshelf);
        }

        item.softDelete();
        bookshelfRepository.saveItem(item);
        normalizeItemOrders(command.shelfId());
        return loadBookshelfResult(bookshelf);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookshelfResult reorderItems(ReorderBookshelfItemsCommand command) {
        validateId(command.userId(), "userId");
        validateId(command.shelfId(), "shelfId");

        Bookshelf bookshelf = getOwnedBookshelfOrThrow(command.shelfId(), command.userId());
        List<BookshelfItem> activeItems = bookshelfRepository.findAllItemsByShelfIdActive(command.shelfId());
        validateReorderRequest(activeItems, command.itemIds());
        java.util.Map<UUID, BookshelfItem> itemsById = activeItems.stream()
                .collect(java.util.stream.Collectors.toMap(BookshelfItem::getId, java.util.function.Function.identity()));

        for (int index = 0; index < command.itemIds().size(); index++) {
            UUID itemId = command.itemIds().get(index);
            BookshelfItem item = itemsById.get(itemId);
            if (item != null) {
                item.moveTo(index);
            }
        }

        bookshelfRepository.saveAllItems(activeItems);
        return loadBookshelfResult(bookshelf);
    }

    private Bookshelf getOwnedBookshelfOrThrow(UUID shelfId, UUID userId) {
        return bookshelfRepository.findByIdAndUserIdActive(shelfId, userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOKSHELF_NOT_FOUND));
    }

    private BookshelfResult loadBookshelfResult(Bookshelf bookshelf) {
        return bookshelfAssembler.toResult(
                bookshelf,
                bookshelfRepository.findAllItemsByShelfIdActive(bookshelf.getId())
        );
    }

    private void normalizeItemOrders(UUID shelfId) {
        List<BookshelfItem> activeItems = bookshelfRepository.findAllItemsByShelfIdActive(shelfId);
        boolean changed = false;
        for (int index = 0; index < activeItems.size(); index++) {
            BookshelfItem item = activeItems.get(index);
            if (item.getSortOrder() != index) {
                item.moveTo(index);
                changed = true;
            }
        }

        if (changed) {
            bookshelfRepository.saveAllItems(activeItems);
        }
    }

    private void validateReorderRequest(List<BookshelfItem> activeItems, List<UUID> itemIds) {
        if (activeItems.size() != itemIds.size()) {
            throw new ApplicationException(ApplicationErrorCode.BOOKSHELF_REORDER_INVALID);
        }

        Set<UUID> requestedIds = new LinkedHashSet<>(itemIds);
        if (requestedIds.size() != itemIds.size()) {
            throw new ApplicationException(ApplicationErrorCode.BOOKSHELF_REORDER_INVALID);
        }

        Set<UUID> activeIds = activeItems.stream()
                .map(BookshelfItem::getId)
                .collect(java.util.stream.Collectors.toSet());
        if (!activeIds.equals(requestedIds)) {
            throw new ApplicationException(ApplicationErrorCode.BOOKSHELF_REORDER_INVALID);
        }
    }

    private void validateId(UUID value, String fieldName) {
        if (value == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, fieldName);
        }
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }
}
