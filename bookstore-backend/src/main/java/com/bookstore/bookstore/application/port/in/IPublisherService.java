package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreatePublisherCommand;
import com.bookstore.bookstore.application.command.DeletePublisherCommand;
import com.bookstore.bookstore.application.command.UpdatePublisherCommand;
import com.bookstore.bookstore.domain.model.Publisher;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.UUID;

public interface IPublisherService {

    List<Publisher> getAll();

    PageSliceResult<Publisher> getAll(int page, int size);

    List<Publisher> getAllIncludingDeleted();

    Publisher getById(UUID publisherId);

    Publisher getByIdIncludingDeleted(UUID publisherId);

    Publisher create(CreatePublisherCommand command);

    Publisher update(UpdatePublisherCommand command);

    void delete(DeletePublisherCommand command);
}
