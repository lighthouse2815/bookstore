package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.ReturnRequest;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReturnRequestJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReturnRequestPersistenceMapper {

    public ReturnRequest toDomain(ReturnRequestJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ReturnRequest(
                entity.getId(),
                entity.getOrder().getId(),
                entity.getUser().getId(),
                entity.getReason(),
                entity.getStatus(),
                entity.getAdminNote(),
                entity.getRequestedRefundAmount(),
                entity.getApprovedRefundAmount(),
                entity.getProcessedByUser() == null ? null : entity.getProcessedByUser().getId(),
                entity.getProcessedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(
            ReturnRequest returnRequest,
            ReturnRequestJpaEntity entity,
            OrderJpaEntity order,
            UserJpaEntity user,
            UserJpaEntity processedByUser
    ) {
        entity.setId(returnRequest.getId());
        entity.setOrder(order);
        entity.setUser(user);
        entity.setReason(returnRequest.getReason());
        entity.setStatus(returnRequest.getStatus());
        entity.setAdminNote(returnRequest.getAdminNote());
        entity.setRequestedRefundAmount(returnRequest.getRequestedRefundAmount());
        entity.setApprovedRefundAmount(returnRequest.getApprovedRefundAmount());
        entity.setProcessedByUser(processedByUser);
        entity.setProcessedAt(returnRequest.getProcessedAt());
        entity.setCreatedAt(returnRequest.getCreatedAt());
        entity.setUpdatedAt(returnRequest.getUpdatedAt());
        entity.setDeletedAt(returnRequest.getDeletedAt());
    }
}
