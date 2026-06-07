package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateBookCommand;
import com.bookstore.bookstore.application.command.DeleteBookCommand;
import com.bookstore.bookstore.application.command.UpdateBookCommand;
import com.bookstore.bookstore.domain.model.Book;
import java.util.List;
import java.util.UUID;

public interface IBookService {

    List<Book> getAll();

    List<Book> getAllIncludingDeleted();

    Book getById(UUID bookId);

    Book getByIdIncludingDeleted(UUID bookId);

    List<Book> search(String keyword);

    Book create(CreateBookCommand command);

    Book update(UpdateBookCommand command);

    void delete(DeleteBookCommand command);
}
