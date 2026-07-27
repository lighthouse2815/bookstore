package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateDigitalAssetCommand;
import com.bookstore.bookstore.application.command.DeleteDigitalAssetCommand;
import com.bookstore.bookstore.application.command.UpdateDigitalAssetCommand;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.PublicDigitalAssetCatalogItemResult;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import java.util.List;
import java.util.UUID;

public interface IDigitalAssetService {

    List<DigitalAsset> getPublishedByBookId(UUID bookId);

    PageSliceResult<PublicDigitalAssetCatalogItemResult> getPublishedCatalog(
            String keyword,
            UUID categoryId,
            PageQuery pageQuery
    );

    List<DigitalAsset> getAllByBookIdForAdmin(UUID bookId);

    DigitalAsset create(CreateDigitalAssetCommand command);

    DigitalAsset update(UpdateDigitalAssetCommand command);

    void delete(DeleteDigitalAssetCommand command);
}
