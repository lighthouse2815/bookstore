package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.Refund;
import com.bookstore.bookstore.infrastructure.persistence.entity.RefundJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class RefundPersistenceMapper {
    public Refund toDomain(RefundJpaEntity entity) {
        if (entity == null) return null;
        return new Refund(entity.getId(), entity.getOrderId(), entity.getPaymentId(), entity.getReturnRequestId(),
                entity.getAmount(), entity.getCurrency(), entity.getReason(), entity.getMethod(), entity.getStatus(),
                entity.getExternalReference(), entity.getEvidenceUrl(), entity.getEvidenceMetadata(), entity.getIdempotencyKey(),
                entity.getRequestedBy(), entity.getApprovedBy(), entity.getProcessedBy(), entity.getRequestedAt(),
                entity.getApprovedAt(), entity.getProcessedAt(), entity.getFailureReason(), entity.getCreatedAt(),
                entity.getUpdatedAt(), entity.getVersion());
    }
    public void copyToEntity(Refund refund, RefundJpaEntity entity) {
        entity.setId(refund.getId());
        entity.setOrderId(refund.getOrderId());
        entity.setPaymentId(refund.getPaymentId());
        entity.setReturnRequestId(refund.getReturnRequestId());
        entity.setAmount(refund.getAmount());
        entity.setCurrency(refund.getCurrency());
        entity.setReason(refund.getReason());
        entity.setMethod(refund.getMethod());
        entity.setStatus(refund.getStatus());
        entity.setExternalReference(refund.getExternalReference());
        entity.setEvidenceUrl(refund.getEvidenceUrl());
        entity.setEvidenceMetadata(refund.getEvidenceMetadata());
        entity.setIdempotencyKey(refund.getIdempotencyKey());
        entity.setRequestedBy(refund.getRequestedBy());
        entity.setApprovedBy(refund.getApprovedBy());
        entity.setProcessedBy(refund.getProcessedBy());
        entity.setRequestedAt(refund.getRequestedAt());
        entity.setApprovedAt(refund.getApprovedAt());
        entity.setProcessedAt(refund.getProcessedAt());
        entity.setFailureReason(refund.getFailureReason());
        entity.setCreatedAt(refund.getCreatedAt());
        entity.setUpdatedAt(refund.getUpdatedAt());
        entity.setVersion(refund.getVersion());
    }
}
