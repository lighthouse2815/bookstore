package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.enums.OtpPurpose;
import com.bookstore.bookstore.domain.model.UserOtp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IUserOtpRepository {

    Optional<UserOtp> findLatestByUserIdAndPurpose(UUID userId, OtpPurpose purpose);

    Optional<UserOtp> findLatestPendingByUserIdAndPurpose(UUID userId, OtpPurpose purpose);

    Optional<UserOtp> findLatestPendingByUserIdAndPurposeForUpdate(UUID userId, OtpPurpose purpose);

    Optional<UserOtp> findOldestByUserIdAndPurposeCreatedAfter(UUID userId, OtpPurpose purpose, Instant createdAfter);

    long countByUserIdAndPurposeCreatedAfter(UUID userId, OtpPurpose purpose, Instant createdAfter);

    void invalidatePendingByUserIdAndPurpose(UUID userId, OtpPurpose purpose, Instant invalidatedAt);

    void invalidateActiveByUserIdAndPurpose(UUID userId, OtpPurpose purpose, Instant invalidatedAt);

    UserOtp save(UserOtp userOtp);
}
