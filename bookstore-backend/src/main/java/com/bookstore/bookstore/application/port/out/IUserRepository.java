package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserRepository {

    List<User> findAllActive();

    PageSliceResult<User> findPageByRoleNameActive(String roleName, int page, int size);

    List<User> findAllIncludingDeleted();

    Optional<User> findByIdActive(UUID userId);

    Optional<User> findByIdIncludingDeleted(UUID userId);

    Optional<User> findByUsernameActive(String username);

    Optional<User> findByUsernameIncludingDeleted(String username);

    Optional<User> findByEmailIncludingDeleted(String email);

    boolean existsByIdIncludingDeleted(UUID userId);

    boolean existsByUsernameIncludingDeleted(String username);

    boolean existsByPhoneNumberIncludingDeleted(String phoneNumber);

    boolean existsByEmailIncludingDeleted(String email);

    long countNewCustomersBetween(Instant fromInclusive, Instant toExclusive);

    void deleteById(UUID userId);

    User save(User user);
}
