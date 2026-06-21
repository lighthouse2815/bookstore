package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CreateDigitalAssetCommand;
import com.bookstore.bookstore.application.command.DeleteDigitalAssetCommand;
import com.bookstore.bookstore.application.command.UpdateDigitalAssetCommand;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import java.util.List;
import java.util.UUID;

public interface IDigitalAssetService {

    List<DigitalAsset> getPublishedByBookId(UUID bookId);

    List<DigitalAsset> getAllByBookIdForAdmin(UUID bookId);

    DigitalAsset create(CreateDigitalAssetCommand command);

    DigitalAsset update(UpdateDigitalAssetCommand command);

    void delete(DeleteDigitalAssetCommand command);
}
