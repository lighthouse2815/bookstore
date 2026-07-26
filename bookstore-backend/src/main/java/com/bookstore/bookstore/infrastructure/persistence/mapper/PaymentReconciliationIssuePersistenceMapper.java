package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.PaymentReconciliationIssue;
import com.bookstore.bookstore.infrastructure.persistence.entity.PaymentReconciliationIssueJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentReconciliationIssuePersistenceMapper {
    public PaymentReconciliationIssue toDomain(PaymentReconciliationIssueJpaEntity entity) {
        return entity == null ? null : new PaymentReconciliationIssue(
                entity.getId(), entity.getPaymentId(), entity.getOrderId(), entity.getIssueType(),
                entity.getExpectedAmount(), entity.getReceivedAmount(), entity.getExternalTransactionId(),
                entity.getDeduplicationKey(), entity.getDetails(), entity.getStatus(), entity.getDetectedAt(),
                entity.getResolvedAt(), entity.getResolvedBy(), entity.getResolutionNote(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
    public void copyToEntity(PaymentReconciliationIssue source, PaymentReconciliationIssueJpaEntity target) {
        target.setId(source.getId()); target.setPaymentId(source.getPaymentId()); target.setOrderId(source.getOrderId());
        target.setIssueType(source.getIssueType()); target.setExpectedAmount(source.getExpectedAmount()); target.setReceivedAmount(source.getReceivedAmount());
        target.setExternalTransactionId(source.getExternalTransactionId()); target.setDeduplicationKey(source.getDeduplicationKey());
        target.setDetails(source.getDetails()); target.setStatus(source.getStatus()); target.setDetectedAt(source.getDetectedAt());
        target.setResolvedAt(source.getResolvedAt()); target.setResolvedBy(source.getResolvedBy()); target.setResolutionNote(source.getResolutionNote());
        target.setCreatedAt(source.getCreatedAt()); target.setUpdatedAt(source.getUpdatedAt());
    }
}
