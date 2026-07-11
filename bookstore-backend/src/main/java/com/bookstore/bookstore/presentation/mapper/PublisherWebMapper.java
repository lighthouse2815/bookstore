package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreatePublisherCommand;
import com.bookstore.bookstore.application.command.DeletePublisherCommand;
import com.bookstore.bookstore.application.command.UpdatePublisherCommand;
import com.bookstore.bookstore.domain.model.Publisher;
import com.bookstore.bookstore.presentation.request.CreatePublisherRequest;
import com.bookstore.bookstore.presentation.request.UpdatePublisherRequest;
import com.bookstore.bookstore.presentation.response.PublisherResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PublisherWebMapper {

    public CreatePublisherCommand toCreateCommand(CreatePublisherRequest request) {
        return new CreatePublisherCommand(
                request.name(),
                request.description(),
                request.logoFileAssetId()
        );
    }

    public UpdatePublisherCommand toUpdateCommand(UUID publisherId, UpdatePublisherRequest request) {
        return new UpdatePublisherCommand(
                publisherId,
                request.name(),
                request.description(),
                request.logoFileAssetId()
        );
    }

    public DeletePublisherCommand toDeleteCommand(UUID publisherId) {
        return new DeletePublisherCommand(publisherId);
    }

    public PublisherResponse toPublisherResponse(Publisher publisher) {
        return new PublisherResponse(
                publisher.getId(),
                publisher.getName(),
                publisher.getDescription(),
                publisher.getLogoFileAssetId(),
                publisher.getLogoUrl(),
                publisher.getCreatedAt(),
                publisher.getUpdatedAt()
        );
    }
}
