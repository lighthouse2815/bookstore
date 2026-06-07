package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Publisher;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPublisherRepository {

    List<Publisher> findAllActive();

    List<Publisher> findAllIncludingDeleted();

    Optional<Publisher> findByIdActive(UUID publisherId);

    Optional<Publisher> findByIdIncludingDeleted(UUID publisherId);

    Optional<Publisher> findByNameActive(String publisherName);

    boolean existsByIdIncludingDeleted(UUID publisherId);

    boolean existsByNameIncludingDeleted(String publisherName);

    Publisher save(Publisher publisher);

    void deleteById(UUID publisherId);
}
