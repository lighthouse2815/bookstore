package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Payment;
import java.util.Optional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IPaymentRepository {

    Optional<Payment> findById(UUID paymentId);

    Optional<Payment> findByIdForUpdate(UUID paymentId);

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByOrderIdForUpdate(UUID orderId);

    Optional<Payment> findByReferenceCode(String referenceCode);

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findPendingSepayByOrderCode(String orderCode);

    Optional<Payment> findPendingSepayByOrderCodeForUpdate(String orderCode);

    Optional<Payment> findPendingSepayByTransferContentInContent(String content);

    Optional<Payment> findPendingSepayByTransferContentInContentForUpdate(String content);

    Optional<Payment> findSepayByOrderCodeForUpdate(String orderCode);

    Optional<Payment> findSepayByTransferContentInContentForUpdateAnyStatus(String content);

    List<UUID> findPendingExpiredIds(Instant now, int limit);

    Payment save(Payment payment);
}
