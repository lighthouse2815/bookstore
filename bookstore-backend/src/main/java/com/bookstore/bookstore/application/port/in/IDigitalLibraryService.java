package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.UpdateReadingProgressCommand;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.DigitalLibraryAssetResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.SignedUrlResult;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.ReadingProgress;
import java.util.List;
import java.util.UUID;

public interface IDigitalLibraryService {

    List<DigitalLibraryAssetResult> getMyLibrary(UUID userId);

    PageSliceResult<DigitalLibraryAssetResult> getMyLibrary(UUID userId, PageQuery pageQuery);

    DigitalLibraryAssetResult getMyAsset(UUID userId, UUID digitalAssetId);

    SignedUrlResult getPublishedSampleUrl(UUID bookId, UUID digitalAssetId);

    SignedUrlResult getMyReadUrl(UUID userId, UUID digitalAssetId);

    SignedUrlResult getMyDownloadUrl(UUID userId, UUID digitalAssetId);

    ReadingProgress updateMyProgress(UpdateReadingProgressCommand command);

    void grantPurchasedAccessForOrder(Order order);

    void revokePurchasedAccessForOrder(UUID orderId);
}
