package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.UpdateUserCommand;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.presentation.request.UpdateUserRequest;
import com.bookstore.bookstore.presentation.response.UserMeResponse;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {

    public UpdateUserCommand toUpdateCommand(UUID userId, UpdateUserRequest request) {
        return new UpdateUserCommand(
                userId,
                request.username(),
                request.phoneNumber(),
                request.email()
        );
    }

    public UserMeResponse toUserMeResponse(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                user.isLocked(),
                user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
