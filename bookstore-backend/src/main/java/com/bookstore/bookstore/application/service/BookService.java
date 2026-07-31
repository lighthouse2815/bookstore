package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateBookCommand;
import com.bookstore.bookstore.application.command.DeleteBookCommand;
import com.bookstore.bookstore.application.command.BookDetailCommand;
import com.bookstore.bookstore.application.command.BookImageCommand;
import com.bookstore.bookstore.application.command.UpdateBookCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IBookService;
import com.bookstore.bookstore.application.port.out.IAuthorRepository;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICategoryRepository;
import com.bookstore.bookstore.application.port.out.IPublisherRepository;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.BookDetail;
import com.bookstore.bookstore.domain.model.BookImage;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService implements IBookService {

    private final IBookRepository bookRepository;
    private final ICategoryRepository categoryRepository;
    private final IAuthorRepository authorRepository;
    private final IPublisherRepository publisherRepository;
    private final FileAssetPolicyService fileAssetPolicyService;

    @Override
    public List<Book> getAll() {
        return bookRepository.findAllActive();
    }

    @Override
    public List<Book> getAllIncludingDeleted() {
        return bookRepository.findAllIncludingDeleted();
    }

    @Override
    public Book getById(UUID bookId) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }

        return bookRepository.findByIdActive(bookId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));
    }

    @Override
    public Book getByIdIncludingDeleted(UUID bookId) {
        if (bookId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "bookId");
        }

        return bookRepository.findByIdIncludingDeleted(bookId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));
    }

    @Override
    public List<Book> search(String keyword) {
        String normalizedKeyword = StringUtils.trimToNull(keyword);
        if (normalizedKeyword == null) {
            return getAll();
        }
        return bookRepository.searchByKeywordActive(normalizedKeyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Book create(CreateBookCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String title = StringUtils.trimToNull(command.title());
        String isbn = StringUtils.trimToNull(command.isbn());
        String description = StringUtils.trimToNull(command.description());
        BigDecimal price = command.price();
        Integer stockQuantity = command.stockQuantity();
        UUID categoryId = command.categoryId();
        UUID authorId = command.authorId();
        UUID publisherId = command.publisherId();
 
        requireActiveCategory(categoryId);
        requireActiveAuthor(authorId);
        requireActivePublisher(publisherId);

        Instant now = Instant.now();
        UUID bookId = UUID.randomUUID();
        Book book = new Book(
                bookId,
                title,
                isbn,
                description,
                price,
                stockQuantity,
                toBookImages(bookId, command.images(), List.of(), now),
                toBookDetail(bookId, command.detail(), null),
                categoryId,
                authorId,
                publisherId,
                now,
                now,
                null
        );

        return bookRepository.save(book);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Book update(UpdateBookCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Book currentBook = bookRepository.findByIdActive(command.bookId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        String title = StringUtils.trimToNull(command.title());
        String isbn = StringUtils.trimToNull(command.isbn());
        String description = StringUtils.trimToNull(command.description());
        BigDecimal price = command.price();
        Integer stockQuantity = command.stockQuantity();
        UUID categoryId = command.categoryId();
        UUID authorId = command.authorId();
        UUID publisherId = command.publisherId();

        requireActiveCategory(categoryId);
        requireActiveAuthor(authorId);
        requireActivePublisher(publisherId);

        currentBook.updateBook(
                title,
                isbn,
                description,
                price,
                stockQuantity,
                toBookImages(currentBook.getId(), command.images(), currentBook.getImages(), Instant.now()),
                toBookDetail(currentBook.getId(), command.detail(), currentBook.getDetail()),
                categoryId,
                authorId,
                publisherId
        );

        return bookRepository.save(currentBook);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteBookCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Book currentBook = bookRepository.findByIdActive(command.bookId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND));

        currentBook.softDelete();
        bookRepository.save(currentBook);
    }

    private void requireActiveCategory(UUID categoryId) {
        categoryRepository.findByIdActive(categoryId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CATEGORY_NOT_FOUND));
    }

    private void requireActiveAuthor(UUID authorId) {
        authorRepository.findByIdActive(authorId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTHOR_NOT_FOUND));
    }

    private void requireActivePublisher(UUID publisherId) {
        publisherRepository.findByIdActive(publisherId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PUBLISHER_NOT_FOUND));
    }

    private List<BookImage> toBookImages(
            UUID bookId,
            List<BookImageCommand> imageCommands,
            List<BookImage> currentImages,
            Instant now
    ) {
        if (imageCommands == null || imageCommands.isEmpty()) {
            return List.of();
        }

        List<BookImage> existingImages = currentImages == null ? List.of() : currentImages;
        Map<UUID, BookImage> existingImagesById = existingImages.stream()
                .collect(Collectors.toMap(BookImage::getId, Function.identity()));

        return java.util.stream.IntStream.range(0, imageCommands.size())
                .mapToObj(index -> toBookImage(
                        bookId,
                        imageCommands.get(index),
                        index,
                        existingImages,
                        existingImagesById,
                        now
                ))
                .toList();
    }

    private BookImage toBookImage(
            UUID bookId,
            BookImageCommand imageCommand,
            int index,
            List<BookImage> currentImages,
            Map<UUID, BookImage> existingImagesById,
            Instant now
    ) {
        if (imageCommand == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "images");
        }

        UUID imageId = resolveImageId(imageCommand, index, currentImages);
        BookImage currentImage = existingImagesById.get(imageId);
        Instant createdAt = currentImage == null ? now : currentImage.getCreatedAt();

        return new BookImage(
                imageId,
                bookId,
                fileAssetPolicyService.requireActiveAsset(
                        imageCommand.fileAssetId(),
                        FilePurpose.BOOK_IMAGE,
                        FileVisibility.PUBLIC
                ),
                imageCommand.primaryImage() != null ? imageCommand.primaryImage() : false,
                imageCommand.sortOrder() != null ? imageCommand.sortOrder() : index,
                StringUtils.trimToNull(imageCommand.altText()),
                createdAt
        );
    }

    private UUID resolveImageId(BookImageCommand imageCommand, int index, List<BookImage> currentImages) {
        if (imageCommand.id() != null) {
            return imageCommand.id();
        }
        if (index < currentImages.size()) {
            return currentImages.get(index).getId();
        }
        return UUID.randomUUID();
    }

    private BookDetail toBookDetail(UUID bookId, BookDetailCommand detailCommand, BookDetail currentDetail) {
        if (detailCommand == null) {
            return null;
        }

        UUID detailId = detailCommand.id();
        if (detailId == null && currentDetail != null) {
            detailId = currentDetail.getId();
        }
        if (detailId == null) {
            detailId = UUID.randomUUID();
        }

        return new BookDetail(
                detailId,
                bookId,
                detailCommand.pageCount(),
                detailCommand.publicationYear(),
                StringUtils.trimToNull(detailCommand.language()),
                StringUtils.trimToNull(detailCommand.coverType()),
                StringUtils.trimToNull(detailCommand.dimensions()),
                detailCommand.weight(),
                StringUtils.trimToNull(detailCommand.translator()),
                StringUtils.trimToNull(detailCommand.edition())
        );
    }
}
