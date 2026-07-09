package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.ApproveReturnRequestCommand;
import com.bookstore.bookstore.application.command.CancelReturnRequestCommand;
import com.bookstore.bookstore.application.command.CreateReturnRequestCommand;
import com.bookstore.bookstore.application.command.RejectReturnRequestCommand;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReturnRequestResult;
import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import java.util.List;
import java.util.UUID;

public interface IReturnRequestService {

    ReturnRequestResult create(CreateReturnRequestCommand command);

    List<ReturnRequestResult> getMyRequests(UUID userId, ReturnRequestStatus status, UUID orderId);

    PageSliceResult<ReturnRequestResult> getMyRequests(
            UUID userId,
            int page,
            int size,
            ReturnRequestStatus status,
            UUID orderId
    );

    ReturnRequestResult getMyRequest(UUID userId, UUID requestId);

    ReturnRequestResult cancel(CancelReturnRequestCommand command);

    List<ReturnRequestResult> getAll(ReturnRequestStatus status, UUID userId, UUID orderId);

    PageSliceResult<ReturnRequestResult> getAll(
            int page,
            int size,
            ReturnRequestStatus status,
            UUID userId,
            UUID orderId
    );

    ReturnRequestResult getById(UUID requestId);

    ReturnRequestResult approve(ApproveReturnRequestCommand command);

    ReturnRequestResult reject(RejectReturnRequestCommand command);
}
