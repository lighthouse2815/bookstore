package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IUserDigitalAccessRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.DigitalAccessStatus;
import com.bookstore.bookstore.domain.enums.DigitalAccessType;
import com.bookstore.bookstore.domain.model.UserDigitalAccess;
import com.bookstore.bookstore.infrastructure.persistence.entity.DigitalAssetJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserDigitalAccessJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.UserDigitalAccessPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.DigitalAssetJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserDigitalAccessJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserDigitalAccessRepositoryAdapter implements IUserDigitalAccessRepository {

    private final UserDigitalAccessJpaRepository userDigitalAccessJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final DigitalAssetJpaRepository digitalAssetJpaRepository;
    private final UserDigitalAccessPersistenceMapper userDigitalAccessPersistenceMapper;

    @Override
    public Optional<UserDigitalAccess> findByIdActive(UUID userDigitalAccessId) {
        return userDigitalAccessJpaRepository
                .findByIdAndDeletedAtIsNullAndUser_DeletedAtIsNullAndDigitalAsset_DeletedAtIsNull(userDigitalAccessId)
                .map(userDigitalAccessPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserDigitalAccess> findByIdIncludingDeleted(UUID userDigitalAccessId) {
        return userDigitalAccessJpaRepository.findById(userDigitalAccessId)
                .map(userDigitalAccessPersistenceMapper::toDomain);
    }

    @Override
    public List<UserDigitalAccess> findAllByUserIdActive(UUID userId) {
        return userDigitalAccessJpaRepository
                .findAllByUser_IdAndDeletedAtIsNullAndUser_DeletedAtIsNullAndDigitalAsset_DeletedAtIsNullOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(userDigitalAccessPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public PageSliceResult<UserDigitalAccess> findAccessiblePageByUserId(UUID userId, Instant now, int page, int size) {
        var resultPage = userDigitalAccessJpaRepository.findAccessiblePageByUserId(
                userId,
                DigitalAccessStatus.ACTIVE,
                now,
                PageRequest.of(page, size)
        );
        return new PageSliceResult<>(
                resultPage.stream()
                        .map(userDigitalAccessPersistenceMapper::toDomain)
                        .toList(),
                resultPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public List<UserDigitalAccess> findAllByUserIdIncludingDeleted(UUID userId) {
        return userDigitalAccessJpaRepository.findAllByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(userDigitalAccessPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<UserDigitalAccess> findAllByUserIdAndDigitalAssetIdActive(UUID userId, UUID digitalAssetId) {
        return userDigitalAccessJpaRepository
                .findAllByUser_IdAndDigitalAsset_IdAndDeletedAtIsNullAndUser_DeletedAtIsNullAndDigitalAsset_DeletedAtIsNull(
                        userId,
                        digitalAssetId
                )
                .stream()
                .map(userDigitalAccessPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<UserDigitalAccess> findAllBySourceOrderIdActive(UUID sourceOrderId) {
        return userDigitalAccessJpaRepository
                .findAllBySourceOrderIdAndDeletedAtIsNullAndUser_DeletedAtIsNullAndDigitalAsset_DeletedAtIsNullOrderByCreatedAtDesc(
                        sourceOrderId
                )
                .stream()
                .map(userDigitalAccessPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserDigitalAccess> findLatestByUserIdAndDigitalAssetIdAndAccessType(
            UUID userId,
            UUID digitalAssetId,
            DigitalAccessType accessType
    ) {
        return userDigitalAccessJpaRepository
                .findFirstByUser_IdAndDigitalAsset_IdAndAccessTypeOrderByCreatedAtDesc(
                        userId,
                        digitalAssetId,
                        accessType
                )
                .map(userDigitalAccessPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByUserIdAndDigitalAssetIdAndStatusActive(
            UUID userId,
            UUID digitalAssetId,
            DigitalAccessStatus status
    ) {
        return userDigitalAccessJpaRepository
                .existsByUser_IdAndDigitalAsset_IdAndStatusAndDeletedAtIsNullAndUser_DeletedAtIsNullAndDigitalAsset_DeletedAtIsNull(
                        userId,
                        digitalAssetId,
                        status
                );
    }

    @Override
    public UserDigitalAccess save(UserDigitalAccess userDigitalAccess) {
        UserDigitalAccessJpaEntity entity = userDigitalAccessJpaRepository.findById(userDigitalAccess.getId())
                .orElseGet(UserDigitalAccessJpaEntity::new);
        UserJpaEntity user = userJpaRepository.getReferenceById(userDigitalAccess.getUserId());
        DigitalAssetJpaEntity digitalAsset = digitalAssetJpaRepository.getReferenceById(
                userDigitalAccess.getDigitalAssetId()
        );

        userDigitalAccessPersistenceMapper.copyToEntity(userDigitalAccess, entity, user, digitalAsset);
        return userDigitalAccessPersistenceMapper.toDomain(userDigitalAccessJpaRepository.save(entity));
    }
}
