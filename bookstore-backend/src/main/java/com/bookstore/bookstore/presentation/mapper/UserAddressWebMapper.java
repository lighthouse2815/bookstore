package com.bookstore.bookstore.presentation.mapper;

import com.bookstore.bookstore.application.command.CreateUserAddressCommand;
import com.bookstore.bookstore.application.command.DeleteUserAddressCommand;
import com.bookstore.bookstore.application.command.SetDefaultUserAddressCommand;
import com.bookstore.bookstore.application.command.UpdateUserAddressCommand;
import com.bookstore.bookstore.domain.model.UserAddress;
import com.bookstore.bookstore.presentation.request.CreateUserAddressRequest;
import com.bookstore.bookstore.presentation.request.UpdateUserAddressRequest;
import com.bookstore.bookstore.presentation.response.UserAddressResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserAddressWebMapper {

    public CreateUserAddressCommand toCreateCommand(UUID userId, CreateUserAddressRequest request) {
        return new CreateUserAddressCommand(
                userId,
                request.receiverName(),
                request.receiverPhone(),
                request.receiverAddress()
        );
    }

    public UpdateUserAddressCommand toUpdateCommand(UUID userId, UUID addressId, UpdateUserAddressRequest request) {
        return new UpdateUserAddressCommand(
                userId,
                addressId,
                request.receiverName(),
                request.receiverPhone(),
                request.receiverAddress()
        );
    }

    public DeleteUserAddressCommand toDeleteCommand(UUID userId, UUID addressId) {
        return new DeleteUserAddressCommand(userId, addressId);
    }

    public SetDefaultUserAddressCommand toSetDefaultCommand(UUID userId, UUID addressId) {
        return new SetDefaultUserAddressCommand(userId, addressId);
    }

    public UserAddressResponse toResponse(UserAddress userAddress) {
        return new UserAddressResponse(
                userAddress.getId(),
                userAddress.getUserId(),
                userAddress.getReceiverName(),
                userAddress.getReceiverPhone(),
                userAddress.getReceiverAddress(),
                userAddress.isDefaultAddress(),
                userAddress.getCreatedAt(),
                userAddress.getUpdatedAt()
        );
    }
}
