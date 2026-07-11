package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.RefundMethod;
import com.bookstore.bookstore.domain.enums.RefundStatus;
import com.bookstore.bookstore.domain.model.Refund;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface IRefundRepository {
    Optional<Refund> findById(UUID id);
    Optional<Refund> findByIdForUpdate(UUID id);
    Optional<Refund> findByOrderIdAndIdempotencyKey(UUID orderId, String idempotencyKey);
    PageSliceResult<Refund> findPage(int page, int size, RefundStatus status, RefundMethod method, Instant from, Instant to);
    BigDecimal sumAmountByPaymentIdAndStatuses(UUID paymentId, Collection<RefundStatus> statuses);
    Refund save(Refund refund);
}
