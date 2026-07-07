package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.enums.DigitalAccessType;
import com.bookstore.bookstore.domain.model.UserDigitalAccess;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserDigitalAccessRepository {

    Optional<UserDigitalAccess> findByIdActive(UUID userDigitalAccessId);

    Optional<UserDigitalAccess> findByIdIncludingDeleted(UUID userDigitalAccessId);

    List<UserDigitalAccess> findAllByUserIdActive(UUID userId);

    PageSliceResult<UserDigitalAccess> findAccessiblePageByUserId(UUID userId, Instant now, int page, int size);

    List<UserDigitalAccess> findAllByUserIdIncludingDeleted(UUID userId);

    List<UserDigitalAccess> findAllByUserIdAndDigitalAssetIdActive(UUID userId, UUID digitalAssetId);

    List<UserDigitalAccess> findAllBySourceOrderIdActive(UUID sourceOrderId);

    Optional<UserDigitalAccess> findLatestByUserIdAndDigitalAssetIdAndAccessType(
            UUID userId,
            UUID digitalAssetId,
            DigitalAccessType accessType
    );

    boolean existsByUserIdAndDigitalAssetIdAndStatusActive(
            UUID userId,
            UUID digitalAssetId,
            DigitalAccessStatus status
    );

    UserDigitalAccess save(UserDigitalAccess userDigitalAccess);
}
