package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.DigitalAsset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDigitalAssetRepository {

    Optional<DigitalAsset> findByIdActive(UUID digitalAssetId);

    Optional<DigitalAsset> findByIdIncludingDeleted(UUID digitalAssetId);

    List<DigitalAsset> findAllByBookIdActive(UUID bookId);

    List<DigitalAsset> findAllByBookIdIncludingDeleted(UUID bookId);

    List<DigitalAsset> findAllByBookIdsActive(List<UUID> bookIds);

    List<DigitalAsset> findAllByIdsActive(List<UUID> digitalAssetIds);

    DigitalAsset save(DigitalAsset digitalAsset);
}
