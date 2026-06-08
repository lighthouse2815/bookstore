package com.bookstore.bookstore.infrastructure.persistence.mapper;

import com.bookstore.bookstore.domain.model.UserAddress;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserAddressJpaEntity;
import com.bookstore.bookstore.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserAddressPersistenceMapper {

    public UserAddress toDomain(UserAddressJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new UserAddress(
                entity.getId(),
                entity.getUser().getId(),
                entity.getReceiverName(),
                entity.getReceiverPhone(),
                entity.getReceiverAddress(),
                entity.isDefaultAddress(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public void copyToEntity(UserAddress userAddress, UserAddressJpaEntity entity, UserJpaEntity user) {
        entity.setId(userAddress.getId());
        entity.setUser(user);
        entity.setReceiverName(userAddress.getReceiverName());
        entity.setReceiverPhone(userAddress.getReceiverPhone());
        entity.setReceiverAddress(userAddress.getReceiverAddress());
        entity.setDefaultAddress(userAddress.isDefaultAddress());
        entity.setCreatedAt(userAddress.getCreatedAt());
        entity.setUpdatedAt(userAddress.getUpdatedAt());
        entity.setDeletedAt(userAddress.getDeletedAt());
    }
}
