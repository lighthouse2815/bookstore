package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserRepository {

    List<User> findAll();

    Optional<User> findById(UUID userId);

    Optional<User> findByUsername(String username);

    boolean existsById(UUID userId);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    void deleteById(UUID userId);

    User save(User user);
}
