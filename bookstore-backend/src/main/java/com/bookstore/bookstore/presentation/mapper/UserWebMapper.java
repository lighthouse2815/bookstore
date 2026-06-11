package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateUserCommand;
import com.bookstore.bookstore.application.command.UpdateStaffUserCommand;
import com.bookstore.bookstore.application.command.UpdateUserCommand;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.presentation.request.CreateUserRequest;
import com.bookstore.bookstore.presentation.request.UpdateStaffUserRequest;
import com.bookstore.bookstore.presentation.request.UpdateUserRequest;
import com.bookstore.bookstore.presentation.response.UserMeResponse;
import com.bookstore.bookstore.presentation.response.UserResponse;
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

    public CreateUserCommand toCreateCommand(CreateUserRequest request) {
        return new CreateUserCommand(
                request.username(),
                request.password(),
                request.phoneNumber(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.avatarUrl(),
                request.gender(),
                request.dateOfBirth(),
                request.roleName()
        );
    }

    public UpdateStaffUserCommand toUpdateStaffCommand(UUID userId, UpdateStaffUserRequest request) {
        return new UpdateStaffUserCommand(
                userId,
                request.phoneNumber(),
                request.email(),
                request.roleNames()
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

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
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
