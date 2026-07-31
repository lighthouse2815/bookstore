package com.bookstore.bookstore.application.port.in;

import com.bookstore.bookstore.application.command.CancelRefundCommand;
import com.bookstore.bookstore.application.command.CreateRefundCommand;
import com.bookstore.bookstore.application.command.FailRefundCommand;
import com.bookstore.bookstore.application.command.SucceedRefundCommand;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.RefundResult;
import com.bookstore.bookstore.domain.enums.RefundMethod;
import com.bookstore.bookstore.domain.enums.RefundStatus;
import java.time.Instant;
import java.util.UUID;

public interface IRefundService {
    RefundResult create(CreateRefundCommand command);
    RefundResult getById(UUID id);
    PageSliceResult<RefundResult> getPage(PageQuery pageQuery, RefundStatus status, RefundMethod method, Instant from, Instant to);
    RefundResult approve(UUID id, UUID approvedBy);
    RefundResult startProcessing(UUID id, UUID processedBy);
    RefundResult succeed(SucceedRefundCommand command);
    RefundResult fail(FailRefundCommand command);
    RefundResult cancel(CancelRefundCommand command);
}
