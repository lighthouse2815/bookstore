package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserRepository {

    List<User> findAllActive();

    List<User> findAllIncludingDeleted();

    Optional<User> findByIdActive(UUID userId);

    Optional<User> findByIdIncludingDeleted(UUID userId);

    Optional<User> findByUsernameActive(String username);

    Optional<User> findByUsernameIncludingDeleted(String username);

    boolean existsByIdIncludingDeleted(UUID userId);

    boolean existsByUsernameIncludingDeleted(String username);

    boolean existsByPhoneNumberIncludingDeleted(String phoneNumber);

    boolean existsByEmailIncludingDeleted(String email);

    void deleteById(UUID userId);

    User save(User user);
}
