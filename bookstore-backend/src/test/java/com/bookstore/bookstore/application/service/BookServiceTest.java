package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.BookImageCommand;
import com.bookstore.bookstore.application.command.CreateBookCommand;
import com.bookstore.bookstore.application.command.UpdateBookCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IAuthorRepository;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICategoryRepository;
import com.bookstore.bookstore.application.port.out.IPublisherRepository;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.domain.model.FileAsset;
import com.bookstore.bookstore.domain.model.Publisher;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private ICategoryRepository categoryRepository;

    @Mock
    private IAuthorRepository authorRepository;

    @Mock
    private IPublisherRepository publisherRepository;

    @Mock
    private FileAssetPolicyService fileAssetPolicyService;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(
                bookRepository,
                categoryRepository,
                authorRepository,
                publisherRepository,
                fileAssetPolicyService
        );
    }

    @Test
    void create_withDeletedCategory_rejectsCategoryNotFound() {
        UUID categoryId = UUID.randomUUID();
        CreateBookCommand command = createCommand(categoryId, UUID.randomUUID(), UUID.randomUUID());
        when(categoryRepository.findByIdActive(categoryId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> bookService.create(command)
        );

        assertEquals(ApplicationErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void create_withDeletedAuthor_rejectsAuthorNotFound() {
        UUID categoryId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        CreateBookCommand command = createCommand(categoryId, authorId, publisherId);
        when(categoryRepository.findByIdActive(categoryId)).thenReturn(Optional.of(category(categoryId)));
        when(authorRepository.findByIdActive(authorId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> bookService.create(command)
        );

        assertEquals(ApplicationErrorCode.AUTHOR_NOT_FOUND, exception.getErrorCode());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void create_withDeletedPublisher_rejectsPublisherNotFound() {
        UUID categoryId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        CreateBookCommand command = createCommand(categoryId, authorId, publisherId);
        when(categoryRepository.findByIdActive(categoryId)).thenReturn(Optional.of(category(categoryId)));
        when(authorRepository.findByIdActive(authorId)).thenReturn(Optional.of(author(authorId)));
        when(publisherRepository.findByIdActive(publisherId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> bookService.create(command)
        );

        assertEquals(ApplicationErrorCode.PUBLISHER_NOT_FOUND, exception.getErrorCode());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void create_withImageFileAsset_createsBookImageBackedByFileAsset() {
        UUID categoryId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID publisherId = UUID.randomUUID();
        UUID fileAssetId = UUID.randomUUID();
        FileAsset fileAsset = publicBookImage(fileAssetId);
        CreateBookCommand command = new CreateBookCommand(
                "Book Title",
                "ISBN-123",
                "Book Description",
                new BigDecimal("10.00"),
                10,
                List.of(new BookImageCommand(null, fileAssetId, true, 0, "Cover")),
                null,
                categoryId,
                authorId,
                publisherId
        );

        when(categoryRepository.findByIdActive(categoryId)).thenReturn(Optional.of(category(categoryId)));
        when(authorRepository.findByIdActive(authorId)).thenReturn(Optional.of(author(authorId)));
        when(publisherRepository.findByIdActive(publisherId)).thenReturn(Optional.of(publisher(publisherId)));
        when(fileAssetPolicyService.requireActiveAsset(
                fileAssetId,
                FilePurpose.BOOK_IMAGE,
                FileVisibility.PUBLIC
        )).thenReturn(fileAsset);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.create(command);

        assertEquals(fileAssetId, result.getImages().getFirst().getFileAssetId());
        assertEquals("https://cdn.example.com/public/books/cover.jpg", result.getPrimaryImageUrl());
    }

    @Test
    void update_withDeletedCategory_rejectsCategoryNotFound() {
        UUID bookId = UUID.randomUUID();
        UUID deletedCategoryId = UUID.randomUUID();
        Book currentBook = book(bookId);
        UpdateBookCommand command = updateCommand(
                bookId,
                deletedCategoryId,
                currentBook.getAuthorId(),
                currentBook.getPublisherId()
        );
        when(bookRepository.findByIdActive(bookId)).thenReturn(Optional.of(currentBook));
        when(categoryRepository.findByIdActive(deletedCategoryId)).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> bookService.update(command)
        );

        assertEquals(ApplicationErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
        verify(bookRepository, never()).save(any(Book.class));
    }

    private static CreateBookCommand createCommand(UUID categoryId, UUID authorId, UUID publisherId) {
        return new CreateBookCommand(
                "Book Title",
                "ISBN-123",
                "Book Description",
                new BigDecimal("10.00"),
                10,
                List.of(),
                null,
                categoryId,
                authorId,
                publisherId
        );
    }

    private static UpdateBookCommand updateCommand(UUID bookId, UUID categoryId, UUID authorId, UUID publisherId) {
        return new UpdateBookCommand(
                bookId,
                "Updated Title",
                "ISBN-123",
                "Updated Description",
                new BigDecimal("12.00"),
                12,
                List.of(),
                null,
                categoryId,
                authorId,
                publisherId
        );
    }

    private static Book book(UUID bookId) {
        Instant now = Instant.EPOCH;
        return new Book(
                bookId,
                "Current Title",
                "ISBN-123",
                "Current Description",
                new BigDecimal("10.00"),
                10,
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

    private static Category category(UUID categoryId) {
        Instant now = Instant.EPOCH;
        return new Category(
                categoryId,
                "Category",
                "Category Description",
                null,
                now,
                now,
                null
        );
    }

    private static Author author(UUID authorId) {
        Instant now = Instant.EPOCH;
        return new Author(
                authorId,
                "Author",
                "Author Biography",
                null,
                null,
                null,
                now,
                now,
                null
        );
    }

    private static Publisher publisher(UUID publisherId) {
        Instant now = Instant.EPOCH;
        return new Publisher(
                publisherId,
                "Publisher",
                "Publisher Description",
                now,
                now,
                null
        );
    }

    private static FileAsset publicBookImage(UUID fileAssetId) {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                fileAssetId,
                FileProvider.R2,
                FilePurpose.BOOK_IMAGE,
                "public-bucket",
                "public/books/cover.jpg",
                "https://cdn.example.com/public/books/cover.jpg",
                "cover.jpg",
                "image/jpeg",
                1024L,
                "checksum",
                FileVisibility.PUBLIC,
                FileStatus.ACTIVE,
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }
}
