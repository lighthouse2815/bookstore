package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.UserAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserAddressRepository {

    List<UserAddress> findAllByUserIdActive(UUID userId);

    Optional<UserAddress> findByIdAndUserIdActive(UUID addressId, UUID userId);

    Optional<UserAddress> findByIdIncludingDeleted(UUID addressId);

    UserAddress save(UserAddress userAddress);
}
