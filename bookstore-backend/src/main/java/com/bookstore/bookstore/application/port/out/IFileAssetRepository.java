package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.FileAsset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IFileAssetRepository {

    Optional<FileAsset> findByIdActive(UUID fileAssetId);

    Optional<FileAsset> findByIdIncludingDeleted(UUID fileAssetId);

    List<FileAsset> findAllByIdsActive(Collection<UUID> fileAssetIds);

    List<String> findUsageReferences(UUID fileAssetId);

    FileAsset save(FileAsset fileAsset);
}
