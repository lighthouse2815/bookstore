package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.UserOtp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IUserOtpRepository {

    Optional<UserOtp> findLatestPendingByUserId(UUID userId);

    void invalidatePendingByUserId(UUID userId, Instant invalidatedAt);

    UserOtp save(UserOtp userOtp);
}
