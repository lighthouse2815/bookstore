package com.bookstore.bookstore.infrastructure.persistence.adapter;

import com.bookstore.bookstore.application.port.out.IUserAddressRepository;
import com.bookstore.bookstore.domain.model.UserAddress;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserAddressJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.mapper.UserAddressPersistenceMapper;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserAddressJpaRepository;
import com.bookstore.bookstore.infrastructure.persistence.repository.UserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserAddressRepositoryAdapter implements IUserAddressRepository {

    private final UserAddressJpaRepository userAddressJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final UserAddressPersistenceMapper userAddressPersistenceMapper;

    @Override
    public List<UserAddress> findAllByUserIdActive(UUID userId) {
        return userAddressJpaRepository.findAllByUserIdActive(userId).stream()
                .map(userAddressPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserAddress> findByIdAndUserIdActive(UUID addressId, UUID userId) {
        return userAddressJpaRepository.findByIdAndUserIdActive(addressId, userId)
                .map(userAddressPersistenceMapper::toDomain);
    }

    @Override
    public Optional<UserAddress> findByIdIncludingDeleted(UUID addressId) {
        return userAddressJpaRepository.findByIdIncludingDeleted(addressId)
                .map(userAddressPersistenceMapper::toDomain);
    }

    @Override
    public UserAddress save(UserAddress userAddress) {
        UserAddressJpaEntity entity = userAddressJpaRepository.findByIdIncludingDeleted(userAddress.getId())
                .orElseGet(UserAddressJpaEntity::new);
        UserJpaEntity user = userJpaRepository.getReferenceById(userAddress.getUserId());
        userAddressPersistenceMapper.copyToEntity(userAddress, entity, user);
        return userAddressPersistenceMapper.toDomain(userAddressJpaRepository.save(entity));
    }
}
