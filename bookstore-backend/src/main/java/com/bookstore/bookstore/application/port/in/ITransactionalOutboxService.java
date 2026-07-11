package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.EnqueueOutboxEventCommand;
import com.bookstore.bookstore.application.result.OutboxEventResult;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.domain.enums.OutboxStatus;
import java.util.UUID;

public interface ITransactionalOutboxService {
    OutboxEventResult enqueue(EnqueueOutboxEventCommand command);
    PageSliceResult<OutboxEventResult> getPage(int page, int size, OutboxStatus status);
    OutboxEventResult getById(UUID id);
    OutboxEventResult retry(UUID id);
}
