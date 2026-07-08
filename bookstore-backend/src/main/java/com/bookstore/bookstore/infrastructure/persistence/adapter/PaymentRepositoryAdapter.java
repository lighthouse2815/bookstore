package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.domain.enums.PaymentProvider;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.infrastructure.persistence.entity.PaymentJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.PaymentJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements IPaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentPersistenceMapper paymentPersistenceMapper;

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return paymentJpaRepository.findById(paymentId)
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId)
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByReferenceCode(String referenceCode) {
        return paymentJpaRepository.findByReferenceCode(referenceCode)
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentJpaRepository.findByTransactionId(transactionId)
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Payment> findPendingSepayByOrderCode(String orderCode) {
        return paymentJpaRepository.findPendingSepayByOrderCode(
                        orderCode,
                        PaymentProvider.SEPAY,
                        PaymentStatus.PENDING
                )
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Payment> findPendingSepayByOrderCodeForUpdate(String orderCode) {
        return paymentJpaRepository.findPendingSepayByOrderCodeForUpdate(
                        orderCode,
                        PaymentProvider.SEPAY,
                        PaymentStatus.PENDING
                )
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Payment> findPendingSepayByTransferContentInContent(String content) {
        return paymentJpaRepository.findPendingSepayByTransferContentInContent(
                        content,
                        PaymentProvider.SEPAY,
                        PaymentStatus.PENDING
                )
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Payment> findPendingSepayByTransferContentInContentForUpdate(String content) {
        return paymentJpaRepository.findPendingSepayByTransferContentInContentForUpdate(
                        content,
                        PaymentProvider.SEPAY,
                        PaymentStatus.PENDING
                )
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = paymentJpaRepository.findById(payment.getId())
                .orElseGet(PaymentJpaEntity::new);
        paymentPersistenceMapper.copyToEntity(payment, entity);
        return paymentPersistenceMapper.toDomain(paymentJpaRepository.save(entity));
    }
}
