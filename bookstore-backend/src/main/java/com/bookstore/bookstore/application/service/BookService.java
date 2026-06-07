package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateBookCommand;
import com.bookstore.bookstore.application.command.DeleteBookCommand;
import com.bookstore.bookstore.application.command.UpdateBookCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IBookService;
import com.bookstore.bookstore.application.port.out.IAuthorRepository;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICategoryRepository;
import com.bookstore.bookstore.application.port.out.IPublisherRepository;
import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.domain.model.Publisher;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
        String description = StringUtils.trimToNull(command.description());
        BigDecimal price = command.price();
        Integer stockQuantity = command.stockQuantity();
        String imageUrl = StringUtils.trimToNull(command.imageUrl());
        UUID categoryId = command.categoryId();
        UUID authorId = command.authorId();
        UUID publisherId = command.publisherId();
 
        requireActiveCategory(categoryId);
        requireActiveAuthor(authorId);
        requireActivePublisher(publisherId);

        Instant now = Instant.now();
        Book book = new Book(
                UUID.randomUUID(),
                title,
                description,
                price,
                stockQuantity,
                imageUrl,
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
        String description = StringUtils.trimToNull(command.description());
        BigDecimal price = command.price();
        Integer stockQuantity = command.stockQuantity();
        String imageUrl = StringUtils.trimToNull(command.imageUrl());
        UUID categoryId = command.categoryId();
        UUID authorId = command.authorId();
        UUID publisherId = command.publisherId();

        requireActiveCategory(categoryId);
        requireActiveAuthor(authorId);
        requireActivePublisher(publisherId);

        currentBook.updateBook(
                title,
                description,
                price,
                stockQuantity,
                imageUrl,
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

    public void xemchitietsach(){
        // TODO : the loai, tac gia , nha xuat ban bi xoa thi van cho hien, dung ham getAllIncludingDeleted
    }

    private Category requireActiveCategory(UUID categoryId) {
        return categoryRepository.findByIdActive(categoryId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.CATEGORY_NOT_FOUND));
    }

    private Author requireActiveAuthor(UUID authorId) {
        return authorRepository.findByIdActive(authorId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.AUTHOR_NOT_FOUND));
    }

    private Publisher requireActivePublisher(UUID publisherId) {
        return publisherRepository.findByIdActive(publisherId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PUBLISHER_NOT_FOUND));
    }
}
