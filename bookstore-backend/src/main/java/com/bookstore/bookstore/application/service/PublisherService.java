package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreatePublisherCommand;
import com.bookstore.bookstore.application.command.DeletePublisherCommand;
import com.bookstore.bookstore.application.command.UpdatePublisherCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IPublisherService;
import com.bookstore.bookstore.application.port.out.IPublisherRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.Publisher;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublisherService implements IPublisherService {

    private final IPublisherRepository publisherRepository;

    @Override
    public List<Publisher> getAll() {
        return publisherRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<Publisher> getAll(int page, int size) {
        validatePageRequest(page, size);
        return publisherRepository.findPageActive(page, size);
    }

    @Override
    public List<Publisher> getAllIncludingDeleted() {
        return publisherRepository.findAllIncludingDeleted();
    }

    @Override
    public Publisher getById(UUID publisherId) {
        if (publisherId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "publisherId");
        }

        return publisherRepository.findByIdActive(publisherId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PUBLISHER_NOT_FOUND));
    }

    @Override
    public Publisher getByIdIncludingDeleted(UUID publisherId) {
        if (publisherId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "publisherId");
        }

        return publisherRepository.findByIdIncludingDeleted(publisherId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PUBLISHER_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Publisher create(CreatePublisherCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String name = StringUtils.trimToNull(command.name());
        String description = StringUtils.trimToNull(command.description());

        if (publisherRepository.existsByNameIncludingDeleted(name)) {
            throw new ApplicationException(ApplicationErrorCode.PUBLISHER_NAME_ALREADY_EXISTS);
        }

        Instant now = Instant.now();
        Publisher publisher = new Publisher(
                UUID.randomUUID(),
                name,
                description,
                now,
                now,
                null
        );

        return publisherRepository.save(publisher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Publisher update(UpdatePublisherCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Publisher currentPublisher = publisherRepository.findByIdActive(command.publisherId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PUBLISHER_NOT_FOUND));

        String name = StringUtils.trimToNull(command.name());
        String description = StringUtils.trimToNull(command.description());

        if (!currentPublisher.getName().equals(name) && publisherRepository.existsByNameIncludingDeleted(name)) {
            throw new ApplicationException(ApplicationErrorCode.PUBLISHER_NAME_ALREADY_EXISTS);
        }

        currentPublisher.updatePublisher(name, description);
        return publisherRepository.save(currentPublisher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeletePublisherCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Publisher currentPublisher = publisherRepository.findByIdActive(command.publisherId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PUBLISHER_NOT_FOUND));

        currentPublisher.softDelete();
        publisherRepository.save(currentPublisher);
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page");
        }
    }
}
