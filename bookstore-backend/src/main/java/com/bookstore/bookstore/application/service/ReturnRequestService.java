package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.assembler.ReturnRequestAssembler;
import com.bookstore.bookstore.application.command.ApproveReturnRequestCommand;
import com.bookstore.bookstore.application.command.CancelReturnRequestCommand;
import com.bookstore.bookstore.application.command.CreateNotificationCommand;
import com.bookstore.bookstore.application.command.CreateReturnRequestCommand;
import com.bookstore.bookstore.application.command.RejectReturnRequestCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.INotificationService;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.port.in.IReturnRequestService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReturnRequestRepository;
import com.bookstore.bookstore.application.port.out.IStockMovementRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.application.result.ReturnRequestResult;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import com.bookstore.bookstore.domain.enums.PurchaseItemType;
import com.bookstore.bookstore.domain.enums.ReturnRequestStatus;
import com.bookstore.bookstore.domain.enums.StockMovementType;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.OrderItem;
import com.bookstore.bookstore.domain.model.ReturnRequest;
import com.bookstore.bookstore.domain.model.StockMovement;
import com.bookstore.bookstore.domain.model.User;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnRequestService implements IReturnRequestService {

    private final IReturnRequestRepository returnRequestRepository;
    private final IOrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final IBookRepository bookRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final INotificationService notificationService;
    private final IOrderTimelineService orderTimelineService;
    private final ReturnRequestAssembler returnRequestAssembler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnRequestResult create(CreateReturnRequestCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Order order = orderRepository.findByIdForUpdate(command.orderId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
        if (!order.getUserId().equals(command.userId())) {
            throw new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_ORDER_NOT_DELIVERED);
        }
        if (returnRequestRepository.existsActiveByOrderIdAndStatuses(
                order.getId(),
                List.of(ReturnRequestStatus.PENDING, ReturnRequestStatus.APPROVED)
        )) {
            throw new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_ALREADY_EXISTS);
        }

        String reason = StringUtils.trimToNull(command.reason());
        if (reason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "reason");
        }

        validateRefundAmount(command.requestedRefundAmount(), order);

        Instant now = Instant.now();
        ReturnRequest returnRequest = new ReturnRequest(
                UUID.randomUUID(),
                order.getId(),
                command.userId(),
                reason,
                ReturnRequestStatus.PENDING,
                null,
                command.requestedRefundAmount(),
                null,
                null,
                null,
                now,
                now,
                null
        );

        ReturnRequest savedRequest = returnRequestRepository.save(returnRequest);
        orderTimelineService.recordReturnRequested(order, savedRequest);
        notifyAdminsAboutNewRequest(order, savedRequest);
        return toResult(savedRequest, order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResult> getMyRequests(UUID userId, ReturnRequestStatus status, UUID orderId) {
        requireUserId(userId);
        return returnRequestRepository.findAllByUserId(userId, status, orderId).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<ReturnRequestResult> getMyRequests(
            UUID userId,
            int page,
            int size,
            ReturnRequestStatus status,
            UUID orderId
    ) {
        requireUserId(userId);
        validatePageRequest(page, size);
        return returnRequestRepository.findPageByUserId(userId, page, size, status, orderId)
                .map(this::toResult);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResult getMyRequest(UUID userId, UUID requestId) {
        requireUserId(userId);
        ReturnRequest returnRequest = returnRequestRepository.findByIdAndUserIdActive(requestId, userId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_NOT_FOUND));
        return toResult(returnRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnRequestResult cancel(CancelReturnRequestCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        ReturnRequest currentRequest = returnRequestRepository.findByIdAndUserIdActive(
                        command.requestId(),
                        command.userId()
                )
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_NOT_FOUND));
        if (currentRequest.getStatus() != ReturnRequestStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_NOT_PENDING);
        }

        currentRequest.cancel();
        ReturnRequest savedRequest = returnRequestRepository.save(currentRequest);
        Order order = orderRepository.findById(savedRequest.getOrderId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
        orderTimelineService.recordReturnCancelled(order, savedRequest);
        return toResult(savedRequest, order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequestResult> getAll(ReturnRequestStatus status, UUID userId, UUID orderId) {
        return returnRequestRepository.findAll(status, userId, orderId).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<ReturnRequestResult> getAll(
            int page,
            int size,
            ReturnRequestStatus status,
            UUID userId,
            UUID orderId
    ) {
        validatePageRequest(page, size);
        return returnRequestRepository.findPageAll(page, size, status, userId, orderId)
                .map(this::toResult);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequestResult getById(UUID requestId) {
        ReturnRequest returnRequest = returnRequestRepository.findByIdActive(requestId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_NOT_FOUND));
        return toResult(returnRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnRequestResult approve(ApproveReturnRequestCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        ReturnRequest currentRequest = returnRequestRepository.findByIdActiveForUpdate(command.requestId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_NOT_FOUND));
        if (currentRequest.getStatus() != ReturnRequestStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_NOT_PENDING);
        }

        Order order = orderRepository.findById(currentRequest.getOrderId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
        BigDecimal approvedRefundAmount = command.approvedRefundAmount() != null
                ? command.approvedRefundAmount()
                : currentRequest.getRequestedRefundAmount();
        validateRefundAmount(approvedRefundAmount, order);

        currentRequest.approve(
                StringUtils.trimToNull(command.adminNote()),
                approvedRefundAmount,
                command.adminUserId(),
                Instant.now()
        );
        ReturnRequest savedRequest = returnRequestRepository.save(currentRequest);

        int restockedQuantity = 0;
        if (command.restock()) {
            restockedQuantity = restockOrder(order, savedRequest, command.adminUserId());
        }

        orderTimelineService.recordReturnApproved(order, savedRequest);
        orderTimelineService.recordRefundInternalApproved(order, savedRequest);
        if (restockedQuantity > 0) {
            orderTimelineService.recordStockRestockedFromReturn(order, savedRequest, restockedQuantity);
        }
        notifyUserAboutStatus(order, savedRequest, true);
        return toResult(savedRequest, order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnRequestResult reject(RejectReturnRequestCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String adminNote = StringUtils.trimToNull(command.adminNote());
        if (adminNote == null) {
            throw new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_REJECT_NOTE_REQUIRED);
        }

        ReturnRequest currentRequest = returnRequestRepository.findByIdActiveForUpdate(command.requestId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_NOT_FOUND));
        if (currentRequest.getStatus() != ReturnRequestStatus.PENDING) {
            throw new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_NOT_PENDING);
        }

        currentRequest.reject(adminNote, command.adminUserId(), Instant.now());
        ReturnRequest savedRequest = returnRequestRepository.save(currentRequest);
        Order order = orderRepository.findById(savedRequest.getOrderId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
        orderTimelineService.recordReturnRejected(order, savedRequest);
        notifyUserAboutStatus(order, savedRequest, false);
        return toResult(savedRequest, order);
    }

    private void requireUserId(UUID userId) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page");
        }
        if (size <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "size");
        }
    }

    private void validateRefundAmount(BigDecimal refundAmount, Order order) {
        if (refundAmount == null) {
            return;
        }
        if (refundAmount.compareTo(BigDecimal.ZERO) < 0
                || refundAmount.compareTo(order.getFinalAmount()) > 0) {
            throw new ApplicationException(ApplicationErrorCode.RETURN_REQUEST_REFUND_AMOUNT_INVALID);
        }
    }

    private int restockOrder(Order order, ReturnRequest returnRequest, UUID adminUserId) {
        Map<UUID, Book> booksById = loadPhysicalOrderBooks(order);
        if (booksById.isEmpty()) {
            return 0;
        }

        Instant now = returnRequest.getProcessedAt() == null ? Instant.now() : returnRequest.getProcessedAt();
        int totalQuantity = 0;

        for (OrderItem item : order.getItems()) {
            if (item.getItemType() != PurchaseItemType.PHYSICAL_BOOK) {
                continue;
            }

            Book book = booksById.get(item.getBookId());
            int beforeQuantity = book.getStockQuantity();
            book.increaseStock(item.getQuantity());
            int afterQuantity = book.getStockQuantity();
            totalQuantity += item.getQuantity();

            stockMovementRepository.save(new StockMovement(
                    UUID.randomUUID(),
                    book.getId(),
                    StockMovementType.ADJUSTMENT,
                    item.getQuantity(),
                    beforeQuantity,
                    afterQuantity,
                    returnRequest.getId(),
                    "RETURN_REQUEST",
                    "Hoàn kho từ yêu cầu trả hàng " + returnRequest.getId(),
                    now,
                    adminUserId
            ));
        }

        booksById.values().forEach(bookRepository::save);
        return totalQuantity;
    }

    private Map<UUID, Book> loadPhysicalOrderBooks(Order order) {
        List<UUID> bookIds = order.getItems().stream()
                .filter(item -> item.getItemType() == PurchaseItemType.PHYSICAL_BOOK)
                .map(OrderItem::getBookId)
                .toList();
        if (bookIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Book> booksById = bookRepository.findAllByIdsIncludingDeletedForUpdate(bookIds).stream()
                .collect(
                        LinkedHashMap::new,
                        (map, book) -> map.put(book.getId(), book),
                        Map::putAll
                );

        for (UUID bookId : bookIds) {
            if (!booksById.containsKey(bookId)) {
                throw new ApplicationException(ApplicationErrorCode.BOOK_NOT_FOUND);
            }
        }

        return booksById;
    }

    private ReturnRequestResult toResult(ReturnRequest returnRequest) {
        Order order = orderRepository.findById(returnRequest.getOrderId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.ORDER_NOT_FOUND));
        return toResult(returnRequest, order);
    }

    private ReturnRequestResult toResult(ReturnRequest returnRequest, Order order) {
        User user = userRepository.findByIdIncludingDeleted(returnRequest.getUserId()).orElse(null);
        User processedByUser = returnRequest.getProcessedBy() == null
                ? null
                : userRepository.findByIdIncludingDeleted(returnRequest.getProcessedBy()).orElse(null);
        return returnRequestAssembler.toResult(returnRequest, order, user, processedByUser);
    }

    private void notifyAdminsAboutNewRequest(Order order, ReturnRequest returnRequest) {
        userRepository.findAllActive().stream()
                .filter(user -> user.hasRole("ADMIN"))
                .forEach(admin -> notifyQuietly(new CreateNotificationCommand(
                        admin.getId(),
                        "Có yêu cầu trả hàng mới",
                        "Đơn hàng " + order.getOrderCode() + " vừa có yêu cầu trả hàng cần xử lý.",
                        "RETURN_REQUEST",
                        "RETURN_REQUEST",
                        returnRequest.getId(),
                        "/admin/return-requests"
                )));
    }

    private void notifyUserAboutStatus(Order order, ReturnRequest returnRequest, boolean approved) {
        String title = approved
                ? "Yêu cầu trả hàng đã được duyệt"
                : "Yêu cầu trả hàng đã bị từ chối";
        String content = approved
                ? "Yêu cầu trả hàng cho đơn " + order.getOrderCode() + " đã được duyệt."
                : "Yêu cầu trả hàng cho đơn " + order.getOrderCode() + " đã bị từ chối.";
        notifyQuietly(new CreateNotificationCommand(
                returnRequest.getUserId(),
                title,
                content,
                "RETURN_REQUEST",
                "RETURN_REQUEST",
                returnRequest.getId(),
                "/return-requests"
        ));
    }

    private void notifyQuietly(CreateNotificationCommand command) {
        try {
            notificationService.create(command);
        } catch (RuntimeException exception) {
            log.warn("Failed to create notification for return request targetId={}", command.targetId(), exception);
        }
    }
}
