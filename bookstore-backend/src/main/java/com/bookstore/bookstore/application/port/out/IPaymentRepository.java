package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.Payment;
import java.util.Optional;
import java.util.UUID;

public interface IPaymentRepository {

    Optional<Payment> findById(UUID paymentId);

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByReferenceCode(String referenceCode);

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findPendingSepayByOrderCode(String orderCode);

    Optional<Payment> findPendingSepayByTransferContentInContent(String content);

    Payment save(Payment payment);
}
