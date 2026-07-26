package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IReturnRequestRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.domain.model.ReturnRequest;
import com.bookstore.bookstore.infrastructure.persistence.entity.OrderJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.ReturnRequestJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.ReturnRequestPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.OrderJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.ReturnRequestJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReturnRequestRepositoryAdapter implements IReturnRequestRepository {

    private final ReturnRequestJpaRepository returnRequestJpaRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ReturnRequestPersistenceMapper returnRequestPersistenceMapper;

    @Override
    public Optional<ReturnRequest> findByIdActive(UUID requestId) {
        return returnRequestJpaRepository.findByIdActive(requestId)
                .map(returnRequestPersistenceMapper::toDomain);
    }

    @Override
    public Optional<ReturnRequest> findByIdActiveForUpdate(UUID requestId) {
        return returnRequestJpaRepository.findByIdActiveForUpdate(requestId)
                .map(returnRequestPersistenceMapper::toDomain);
    }

    @Override
    public Optional<ReturnRequest> findByIdAndUserIdActive(UUID requestId, UUID userId) {
        return returnRequestJpaRepository.findByIdAndUserIdActive(requestId, userId)
                .map(returnRequestPersistenceMapper::toDomain);
    }

    @Override
    public List<ReturnRequest> findAllByUserId(UUID userId, ReturnRequestStatus status, UUID orderId) {
        return returnRequestJpaRepository.findAllByUserId(userId, status, orderId).stream()
                .map(returnRequestPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<ReturnRequest> findPageByUserId(
            UUID userId,
            int page,
            int size,
            ReturnRequestStatus status,
            UUID orderId
    ) {
        var resultPage = returnRequestJpaRepository.findPageByUserId(
                userId,
                status,
                orderId,
                PageRequest.of(page, size)
        );
        return new PageSliceResult<>(
                resultPage.getContent().stream()
                        .map(returnRequestPersistenceMapper::toDomain)
                        .toList(),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public List<ReturnRequest> findAll(ReturnRequestStatus status, UUID userId, UUID orderId) {
        return returnRequestJpaRepository.findAllActive(status, userId, orderId).stream()
                .map(returnRequestPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<ReturnRequest> findPageAll(
            int page,
            int size,
            ReturnRequestStatus status,
            UUID userId,
            UUID orderId
    ) {
        var resultPage = returnRequestJpaRepository.findPageActive(
                status,
                userId,
                orderId,
                PageRequest.of(page, size)
        );
        return new PageSliceResult<>(
                resultPage.getContent().stream()
                        .map(returnRequestPersistenceMapper::toDomain)
                        .toList(),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public boolean existsActiveByOrderIdAndStatuses(UUID orderId, Collection<ReturnRequestStatus> statuses) {
        return returnRequestJpaRepository.existsActiveByOrderIdAndStatuses(orderId, statuses);
    }

    @Override
    public ReturnRequest save(ReturnRequest returnRequest) {
        ReturnRequestJpaEntity entity = returnRequestJpaRepository.findById(returnRequest.getId())
                .orElseGet(ReturnRequestJpaEntity::new);

        OrderJpaEntity order = orderJpaRepository.getReferenceById(returnRequest.getOrderId());
        UserJpaEntity user = userJpaRepository.getReferenceById(returnRequest.getUserId());
        UserJpaEntity processedByUser = returnRequest.getProcessedBy() == null
                ? null
                : userJpaRepository.getReferenceById(returnRequest.getProcessedBy());

        returnRequestPersistenceMapper.copyToEntity(
                returnRequest,
                entity,
                order,
                user,
                processedByUser
        );
        return returnRequestPersistenceMapper.toDomain(returnRequestJpaRepository.save(entity));
    }
}
