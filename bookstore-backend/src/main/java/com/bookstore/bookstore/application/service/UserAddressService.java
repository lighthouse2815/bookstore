package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateUserAddressCommand;
import com.bookstore.bookstore.application.command.DeleteUserAddressCommand;
import com.bookstore.bookstore.application.command.SetDefaultUserAddressCommand;
import com.bookstore.bookstore.application.command.UpdateUserAddressCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IUserAddressService;
import com.bookstore.bookstore.application.port.out.IUserAddressRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.model.UserAddress;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAddressService implements IUserAddressService {

    private final IUserAddressRepository userAddressRepository;
    private final IUserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserAddress> getMyAddresses(UUID userId) {
        return userAddressRepository.findAllByUserIdActive(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAddress getByIdAndUserId(UUID addressId, UUID userId) {
        return userAddressRepository.findByIdAndUserIdActive(addressId, userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_ADDRESS_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddress create(CreateUserAddressCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        if (userRepository.findByIdActive(command.userId()).isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.USER_NOT_FOUND);
        }

        boolean isDefaultAddress = userAddressRepository.findAllByUserIdActive(command.userId()).isEmpty();
        Instant now = Instant.now();
        UserAddress userAddress = new UserAddress(
                UUID.randomUUID(),
                command.userId(),
                StringUtils.trimToNull(command.receiverName()),
                StringUtils.trimToNull(command.receiverPhone()),
                StringUtils.trimToNull(command.receiverAddress()),
                isDefaultAddress,
                now,
                now,
                null
        );

        return userAddressRepository.save(userAddress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddress update(UpdateUserAddressCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UserAddress currentAddress = userAddressRepository.findByIdAndUserIdActive(command.addressId(), command.userId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_ADDRESS_NOT_FOUND));

        currentAddress.updateAddressInfo(
                StringUtils.trimToNull(command.receiverName()),
                StringUtils.trimToNull(command.receiverPhone()),
                StringUtils.trimToNull(command.receiverAddress())
        );
        return userAddressRepository.save(currentAddress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteUserAddressCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UserAddress currentAddress = userAddressRepository.findByIdAndUserIdActive(command.addressId(), command.userId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_ADDRESS_NOT_FOUND));

        boolean wasDefaultAddress = currentAddress.isDefaultAddress();
        currentAddress.softDelete();
        userAddressRepository.save(currentAddress);

        if (!wasDefaultAddress) {
            return;
        }

        userAddressRepository.findAllByUserIdActive(command.userId()).stream()
                .findFirst()
                .ifPresent(address -> userAddressRepository.save(markAsDefault(address)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddress setDefault(SetDefaultUserAddressCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        UUID targetAddressId = userAddressRepository.findByIdAndUserIdActive(command.addressId(), command.userId())
                .map(UserAddress::getId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.USER_ADDRESS_NOT_FOUND));

        List<UserAddress> addresses = userAddressRepository.findAllByUserIdActive(command.userId());
        UserAddress savedDefaultAddress = null;
        for (UserAddress address : addresses) {
            if (address.getId().equals(targetAddressId)) {
                savedDefaultAddress = userAddressRepository.save(markAsDefault(address));
            } else {
                userAddressRepository.save(unmarkAsDefault(address));
            }
        }

        return savedDefaultAddress;
    }

    private UserAddress markAsDefault(UserAddress address) {
        address.markAsDefault();
        return address;
    }

    private UserAddress unmarkAsDefault(UserAddress address) {
        address.unmarkAsDefault();
        return address;
    }
}
