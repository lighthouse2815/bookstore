package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.CreateUserAddressCommand;
import com.bookstore.bookstore.application.command.DeleteUserAddressCommand;
import com.bookstore.bookstore.application.command.SetDefaultUserAddressCommand;
import com.bookstore.bookstore.application.port.out.IUserAddressRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.domain.model.UserAddress;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private IUserAddressRepository userAddressRepository;

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private UserAddressService userAddressService;

    @Test
    void create_firstAddress_marksDefault() {
        User user = user();

        when(userRepository.findByIdActive(user.getId())).thenReturn(Optional.of(user));
        when(userAddressRepository.findAllByUserIdActive(user.getId())).thenReturn(List.of());
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAddress result = userAddressService.create(new CreateUserAddressCommand(
                user.getId(),
                "Receiver Name",
                "0123456789",
                "Receiver Address"
        ));

        assertEquals(true, result.isDefaultAddress());
    }

    @Test
    void setDefault_switchesDefaultAddress() {
        User user = user();
        UserAddress firstAddress = address(user.getId(), true);
        UserAddress secondAddress = address(user.getId(), false);

        when(userAddressRepository.findByIdAndUserIdActive(secondAddress.getId(), user.getId()))
                .thenReturn(Optional.of(secondAddress));
        when(userAddressRepository.findAllByUserIdActive(user.getId())).thenReturn(List.of(firstAddress, secondAddress));
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAddress result = userAddressService.setDefault(
                new SetDefaultUserAddressCommand(user.getId(), secondAddress.getId())
        );

        ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
        verify(userAddressRepository, times(2)).save(captor.capture());
        assertEquals(false, captor.getAllValues().get(0).isDefaultAddress());
        assertEquals(true, captor.getAllValues().get(1).isDefaultAddress());
        assertEquals(true, result.isDefaultAddress());
        assertEquals(secondAddress.getId(), result.getId());
    }

    @Test
    void delete_defaultAddress_promotesAnotherAddress() {
        User user = user();
        UserAddress currentAddress = address(user.getId(), true);
        UserAddress nextAddress = address(user.getId(), false);

        when(userAddressRepository.findByIdAndUserIdActive(currentAddress.getId(), user.getId()))
                .thenReturn(Optional.of(currentAddress));
        when(userAddressRepository.findAllByUserIdActive(user.getId())).thenReturn(List.of(nextAddress));
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userAddressService.delete(new DeleteUserAddressCommand(user.getId(), currentAddress.getId()));

        ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
        verify(userAddressRepository, times(2)).save(captor.capture());
        assertEquals(false, captor.getAllValues().get(0).isDefaultAddress());
        assertEquals(true, captor.getAllValues().get(1).isDefaultAddress());
        assertEquals(nextAddress.getId(), captor.getAllValues().get(1).getId());
    }

    private static User user() {
        Instant now = Instant.EPOCH;
        return new User(
                UUID.randomUUID(),
                "address-user",
                "hashed-password",
                "0123456789",
                "address.user@gmail.com",
                UserStatus.ACTIVE,
                false,
                Set.of(),
                now,
                now,
                null
        );
    }

    private static UserAddress address(UUID userId, boolean defaultAddress) {
        Instant now = Instant.EPOCH;
        return new UserAddress(
                UUID.randomUUID(),
                userId,
                "Receiver Name",
                "0123456789",
                "Receiver Address",
                defaultAddress,
                now,
                now,
                null
        );
    }
}
