package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.assembler.BookshelfAssembler;
import com.bookstore.bookstore.application.command.AddBookshelfItemCommand;
import com.bookstore.bookstore.application.command.CreateBookshelfCommand;
import com.bookstore.bookstore.application.command.ReorderBookshelfItemsCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IBookshelfRepository;
import com.bookstore.bookstore.application.result.BookshelfResult;
import com.bookstore.bookstore.application.result.BookshelfSummaryResult;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Bookshelf;
import com.bookstore.bookstore.domain.model.BookshelfItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookshelfServiceTest {

    @Mock
    private IBookshelfRepository bookshelfRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private BookshelfAssembler bookshelfAssembler;

    @InjectMocks
    private BookshelfService bookshelfService;

    @Test
    void getMyBookshelves_returnsAssemblerSummaryResults() {
        UUID userId = UUID.randomUUID();
        Bookshelf shelf = bookshelf(userId, "Doc sau", false);
        BookshelfSummaryResult summary = new BookshelfSummaryResult(
                shelf.getId(),
                shelf.getName(),
                3L,
                shelf.getCreatedAt(),
                shelf.getUpdatedAt()
        );

        when(bookshelfRepository.findAllByUserIdActive(userId)).thenReturn(List.of(shelf));
        when(bookshelfRepository.countActiveItemsByShelfIds(List.of(shelf.getId()))).thenReturn(
                Map.of(shelf.getId(), 3L)
        );
        when(bookshelfAssembler.toSummaryResults(List.of(shelf), Map.of(shelf.getId(), 3L))).thenReturn(
                List.of(summary)
        );

        List<BookshelfSummaryResult> result = bookshelfService.getMyBookshelves(userId);

        assertEquals(List.of(summary), result);
    }

    @Test
    void create_whenDeletedShelfExists_restoresExistingShelf() {
        UUID userId = UUID.randomUUID();
        Bookshelf deletedShelf = bookshelf(userId, "Qua tang", true);
        BookshelfResult expected = result(deletedShelf.getId(), deletedShelf.getName(), 0);

        when(bookshelfRepository.findByUserIdAndName(userId, "Qua tang")).thenReturn(Optional.of(deletedShelf));
        when(bookshelfRepository.save(deletedShelf)).thenReturn(deletedShelf);
        when(bookshelfRepository.findAllItemsByShelfIdActive(deletedShelf.getId())).thenReturn(List.of());
        when(bookshelfAssembler.toResult(deletedShelf, List.of())).thenReturn(expected);

        BookshelfResult result = bookshelfService.create(new CreateBookshelfCommand(userId, "  Qua tang  "));

        assertFalse(deletedShelf.isDeleted());
        assertEquals(expected, result);
        verify(bookshelfRepository).save(deletedShelf);
    }

    @Test
    void addItem_whenDeletedItemExists_restoresAtNextSortOrder() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Bookshelf shelf = bookshelf(userId, "Doc ngay", false);
        BookshelfItem deletedItem = bookshelfItem(shelf.getId(), bookId, 0, true);
        BookshelfResult expected = result(shelf.getId(), shelf.getName(), 3);

        when(bookshelfRepository.findByIdAndUserIdActive(shelf.getId(), userId)).thenReturn(Optional.of(shelf));
        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(book(bookId)));
        when(bookshelfRepository.findItemByShelfIdAndBookId(shelf.getId(), bookId)).thenReturn(Optional.of(deletedItem));
        when(bookshelfRepository.countActiveItemsByShelfId(shelf.getId())).thenReturn(2L);
        when(bookshelfRepository.findAllItemsByShelfIdActive(shelf.getId())).thenReturn(List.of());
        when(bookshelfAssembler.toResult(shelf, List.of())).thenReturn(expected);

        BookshelfResult result = bookshelfService.addItem(new AddBookshelfItemCommand(shelf.getId(), userId, bookId));

        ArgumentCaptor<BookshelfItem> itemCaptor = ArgumentCaptor.forClass(BookshelfItem.class);
        verify(bookshelfRepository).saveItem(itemCaptor.capture());
        assertFalse(itemCaptor.getValue().isDeleted());
        assertEquals(2, itemCaptor.getValue().getSortOrder());
        assertEquals(expected, result);
    }

    @Test
    void reorderItems_whenRequestDoesNotMatchActiveItems_throwsInvalidReorderError() {
        UUID userId = UUID.randomUUID();
        Bookshelf shelf = bookshelf(userId, "Lich su", false);
        BookshelfItem first = bookshelfItem(shelf.getId(), UUID.randomUUID(), 0, false);
        BookshelfItem second = bookshelfItem(shelf.getId(), UUID.randomUUID(), 1, false);

        when(bookshelfRepository.findByIdAndUserIdActive(shelf.getId(), userId)).thenReturn(Optional.of(shelf));
        when(bookshelfRepository.findAllItemsByShelfIdActive(shelf.getId())).thenReturn(List.of(first, second));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> bookshelfService.reorderItems(new ReorderBookshelfItemsCommand(
                        shelf.getId(),
                        userId,
                        List.of(first.getId())
                ))
        );

        assertEquals(ApplicationErrorCode.BOOKSHELF_REORDER_INVALID, exception.getErrorCode());
    }

    private static Bookshelf bookshelf(UUID userId, String name, boolean deleted) {
        Instant now = Instant.EPOCH;
        return new Bookshelf(
                UUID.randomUUID(),
                userId,
                name,
                now,
                now,
                deleted ? now.plusSeconds(1) : null
        );
    }

    private static BookshelfItem bookshelfItem(UUID shelfId, UUID bookId, int sortOrder, boolean deleted) {
        Instant now = Instant.EPOCH;
        return new BookshelfItem(
                UUID.randomUUID(),
                shelfId,
                bookId,
                sortOrder,
                now,
                now,
                deleted ? now.plusSeconds(1) : null
        );
    }

    private static Book book(UUID bookId) {
        Instant now = Instant.EPOCH;
        return new Book(
                bookId,
                "Sach",
                "ISBN-001",
                "Mo ta",
                new BigDecimal("120000"),
                9,
                List.of(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }

    private static BookshelfResult result(UUID shelfId, String name, int itemCount) {
        return new BookshelfResult(
                shelfId,
                name,
                itemCount,
                List.of(),
                Instant.EPOCH,
                Instant.EPOCH
        );
    }
}
