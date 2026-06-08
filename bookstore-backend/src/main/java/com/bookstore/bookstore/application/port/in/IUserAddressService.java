package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateUserAddressCommand;
import com.bookstore.bookstore.application.command.DeleteUserAddressCommand;
import com.bookstore.bookstore.application.command.SetDefaultUserAddressCommand;
import com.bookstore.bookstore.application.command.UpdateUserAddressCommand;
import com.bookstore.bookstore.domain.model.UserAddress;
import java.util.List;
import java.util.UUID;

public interface IUserAddressService {

    List<UserAddress> getMyAddresses(UUID userId);

    UserAddress getByIdAndUserId(UUID addressId, UUID userId);

    UserAddress create(CreateUserAddressCommand command);

    UserAddress update(UpdateUserAddressCommand command);

    void delete(DeleteUserAddressCommand command);

    UserAddress setDefault(SetDefaultUserAddressCommand command);
}
