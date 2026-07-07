package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.application.result.PageSliceResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDigitalAssetRepository {

    Optional<DigitalAsset> findByIdActive(UUID digitalAssetId);

    Optional<DigitalAsset> findByIdIncludingDeleted(UUID digitalAssetId);

    List<DigitalAsset> findAllByBookIdActive(UUID bookId);

    List<DigitalAsset> findAllByBookIdIncludingDeleted(UUID bookId);

    List<DigitalAsset> findAllByBookIdsActive(List<UUID> bookIds);

    PageSliceResult<DigitalAsset> searchPublishedCatalog(
            String keyword,
            UUID categoryId,
            int page,
            int size
    );

    List<DigitalAsset> findAllByIdsActive(List<UUID> digitalAssetIds);

    DigitalAsset save(DigitalAsset digitalAsset);
}
