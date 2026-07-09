package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.domain.model.ReturnRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IReturnRequestRepository {

    Optional<ReturnRequest> findByIdActive(UUID requestId);

    Optional<ReturnRequest> findByIdActiveForUpdate(UUID requestId);

    Optional<ReturnRequest> findByIdAndUserIdActive(UUID requestId, UUID userId);

    List<ReturnRequest> findAllByUserId(UUID userId, ReturnRequestStatus status, UUID orderId);

    PageSliceResult<ReturnRequest> findPageByUserId(
            UUID userId,
            int page,
            int size,
            ReturnRequestStatus status,
            UUID orderId
    );

    List<ReturnRequest> findAll(ReturnRequestStatus status, UUID userId, UUID orderId);

    PageSliceResult<ReturnRequest> findPageAll(
            int page,
            int size,
            ReturnRequestStatus status,
            UUID userId,
            UUID orderId
    );

    boolean existsActiveByOrderIdAndStatuses(UUID orderId, Collection<ReturnRequestStatus> statuses);

    ReturnRequest save(ReturnRequest returnRequest);
}
