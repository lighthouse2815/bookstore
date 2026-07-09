package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.HandleSepayIpnCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IDigitalLibraryService;
import com.bookstore.bookstore.application.port.in.IPaymentService;
import com.bookstore.bookstore.application.port.in.IOrderTimelineService;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.application.port.out.ISepaySettings;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final IPaymentRepository paymentRepository;
    private final IOrderRepository orderRepository;
    private final IDigitalLibraryService digitalLibraryService;
    private final IOrderTimelineService orderTimelineService;
    private final ISepaySettings sepaySettings;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleSepayIpn(HandleSepayIpnCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        validateWebhookAuthorization(command);
        if (!isSuccessfulIncomingTransfer(command)) {
            log.info(
                    "Ignoring SePay IPN because transfer is not a successful incoming payment: transactionId={}, transferType={}, transferAmount={}",
                    command.transactionId(),
                    command.transferType(),
                    command.transferAmount()
            );
            return;
        }

        if (isDuplicateNotification(command)) {
            log.info(
                    "Ignoring duplicate SePay IPN: transactionId={}, referenceCode={}",
                    command.transactionId(),
                    command.referenceCode()
            );
            return;
        }

        Optional<Payment> pendingPayment = resolvePendingPaymentForUpdate(command);
        if (pendingPayment.isEmpty()) {
            log.warn(
                    "No pending payment matched SePay IPN: transactionId={}, code={}, referenceCode={}, content={}",
                    command.transactionId(),
                    command.code(),
                    command.referenceCode(),
                    command.content()
            );
            return;
        }

        Payment payment = pendingPayment.get();
        if (command.transferAmount() == null || command.transferAmount().compareTo(payment.getAmount()) < 0) {
            log.warn(
                    "Ignoring SePay IPN because transfer amount is lower than expected: transactionId={}, paymentId={}, expectedAmount={}, transferAmount={}",
                    command.transactionId(),
                    payment.getId(),
                    payment.getAmount(),
                    command.transferAmount()
            );
            return;
        }

        Optional<Order> order = orderRepository.findByIdForUpdate(payment.getOrderId());
        if (order.isEmpty()) {
            log.warn(
                    "Matched payment but could not find order: transactionId={}, paymentId={}, orderId={}",
                    command.transactionId(),
                    payment.getId(),
                    payment.getOrderId()
            );
            return;
        }

        Instant now = Instant.now();
        payment.markPaid(
                resolveMerchantId(payment),
                command.transactionId(),
                command.referenceCode(),
                command.gateway(),
                now
        );
        order.get().markPaymentPaid(now);

        paymentRepository.save(payment);
        orderRepository.save(order.get());
        digitalLibraryService.grantPurchasedAccessForOrder(order.get());
        orderTimelineService.recordPaymentPaid(order.get(), payment);
        log.info(
                "Marked payment as paid from SePay IPN: transactionId={}, paymentId={}, orderId={}, paymentStatus={}",
                command.transactionId(),
                payment.getId(),
                order.get().getId(),
                order.get().getPaymentStatus()
        );
    }

    private void validateWebhookAuthorization(HandleSepayIpnCommand command) {
        String configuredApiKey = StringUtils.trimToNull(sepaySettings.webhookApiKey());
        String configuredSecretKey = StringUtils.trimToNull(sepaySettings.secretKey());
        boolean apiKeyConfigured = configuredApiKey != null;
        boolean secretKeyConfigured = configuredSecretKey != null;

        if (!apiKeyConfigured && !secretKeyConfigured) {
            log.error("Rejected SePay IPN because webhook secrets are not configured");
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_WEBHOOK_UNAUTHORIZED);
        }

        boolean apiKeyMatched = apiKeyConfigured
                && ("Apikey " + configuredApiKey).equals(command.authorizationHeader());
        boolean secretKeyMatched = secretKeyConfigured
                && configuredSecretKey.equals(command.secretKeyHeader());

        if (!apiKeyMatched && !secretKeyMatched) {
            log.warn(
                    "Rejected SePay IPN due to invalid authorization headers: transactionId={}, hasAuthorizationHeader={}, hasSecretKeyHeader={}",
                    command.transactionId(),
                    command.authorizationHeader() != null,
                    command.secretKeyHeader() != null
            );
            throw new ApplicationException(ApplicationErrorCode.PAYMENT_WEBHOOK_UNAUTHORIZED);
        }
    }

    private boolean isSuccessfulIncomingTransfer(HandleSepayIpnCommand command) {
        String transferType = command.transferType();
        if (transferType == null) {
            return false;
        }

        if (!"in".equalsIgnoreCase(transferType) && !"credit".equalsIgnoreCase(transferType)) {
            return false;
        }

        BigDecimal transferAmount = command.transferAmount();
        return transferAmount != null && transferAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isDuplicateNotification(HandleSepayIpnCommand command) {
        String transactionId = command.transactionId();
        if (transactionId != null && paymentRepository.findByTransactionId(transactionId).isPresent()) {
            return true;
        }

        String referenceCode = command.referenceCode();
        if (referenceCode == null) {
            return false;
        }

        return paymentRepository.findByReferenceCode(referenceCode)
                .filter(payment -> payment.getStatus() == PaymentStatus.PAID
                        || payment.getTransactionId() != null
                        || !referenceCode.equals(payment.getTransferContent()))
                .isPresent();
    }

    private Optional<Payment> resolvePendingPaymentForUpdate(HandleSepayIpnCommand command) {
        String code = command.code();
        if (code != null) {
            Optional<Payment> byCode = paymentRepository.findPendingSepayByOrderCodeForUpdate(code);
            if (byCode.isPresent()) {
                return byCode;
            }
        }

        String content = command.content();
        if (content == null) {
            return Optional.empty();
        }

        return paymentRepository.findPendingSepayByTransferContentInContentForUpdate(content);
    }

    private String resolveMerchantId(Payment payment) {
        String configuredMerchantId = StringUtils.trimToNull(sepaySettings.merchantId());
        if (configuredMerchantId != null) {
            return configuredMerchantId;
        }
        return payment.getMerchantId();
    }
}
