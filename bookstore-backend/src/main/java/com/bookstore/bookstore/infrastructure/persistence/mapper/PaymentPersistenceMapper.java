package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentPersistenceMapper {

    public Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Payment(
                entity.getId(),
                entity.getOrderId(),
                entity.getProvider(),
                entity.getStatus(),
                entity.getAmount(),
                entity.getMerchantId(),
                entity.getTransactionId(),
                entity.getReferenceCode(),
                entity.getTransferContent(),
                entity.getGateway(),
                entity.getPaidAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void copyToEntity(Payment payment, PaymentJpaEntity entity) {
        entity.setId(payment.getId());
        entity.setOrderId(payment.getOrderId());
        entity.setProvider(payment.getProvider());
        entity.setStatus(payment.getStatus());
        entity.setAmount(payment.getAmount());
        entity.setMerchantId(payment.getMerchantId());
        entity.setTransactionId(payment.getTransactionId());
        entity.setReferenceCode(payment.getReferenceCode());
        entity.setTransferContent(payment.getTransferContent());
        entity.setGateway(payment.getGateway());
        entity.setPaidAt(payment.getPaidAt());
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());
    }
}
