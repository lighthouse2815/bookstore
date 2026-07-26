package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateAuthorCommand;
import com.bookstore.bookstore.application.command.DeleteAuthorCommand;
import com.bookstore.bookstore.application.command.UpdateAuthorCommand;
import com.bookstore.bookstore.domain.model.Author;
import com.bookstore.bookstore.presentation.request.CreateAuthorRequest;
import com.bookstore.bookstore.presentation.request.UpdateAuthorRequest;
import com.bookstore.bookstore.presentation.response.AuthorResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AuthorWebMapper {

    public CreateAuthorCommand toCreateCommand(CreateAuthorRequest request) {
        return new CreateAuthorCommand(
                request.name(),
                request.biography(),
                request.avatarFileAssetId(),
                request.birthYear(),
                request.deathYear()
        );
    }

    public UpdateAuthorCommand toUpdateCommand(UUID authorId, UpdateAuthorRequest request) {
        return new UpdateAuthorCommand(
                authorId,
                request.name(),
                request.biography(),
                request.avatarFileAssetId(),
                request.birthYear(),
                request.deathYear()
        );
    }

    public DeleteAuthorCommand toDeleteCommand(UUID authorId) {
        return new DeleteAuthorCommand(authorId);
    }

    public AuthorResponse toAuthorResponse(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getName(),
                author.getBiography(),
                author.getAvatarFileAssetId(),
                author.getAvatarUrl(),
                author.getBirthYear(),
                author.getDeathYear(),
                author.getCreatedAt(),
                author.getUpdatedAt()
        );
    }
}
