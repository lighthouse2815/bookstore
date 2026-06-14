package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.HandleSepayIpnCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IPaymentService;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.domain.model.Order;
import com.bookstore.bookstore.domain.model.Payment;
import com.bookstore.bookstore.infrastructure.payment.SepayProperties;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final IPaymentRepository paymentRepository;
    private final IOrderRepository orderRepository;
    private final SepayProperties sepayProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleSepayIpn(HandleSepayIpnCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        validateWebhookAuthorization(command);
        if (!isSuccessfulIncomingTransfer(command)) {
            return;
        }

        if (isDuplicateNotification(command)) {
            return;
        }

        Optional<Payment> pendingPayment = resolvePendingPayment(command);
        if (pendingPayment.isEmpty()) {
            return;
        }

        Payment payment = pendingPayment.get();
        if (command.transferAmount() == null || command.transferAmount().compareTo(payment.getAmount()) < 0) {
            return;
        }

        Optional<Order> order = orderRepository.findById(payment.getOrderId());
        if (order.isEmpty()) {
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
    }

    private void validateWebhookAuthorization(HandleSepayIpnCommand command) {
        String configuredApiKey = StringUtils.trimToNull(sepayProperties.webhookApiKey());
        String configuredSecretKey = StringUtils.trimToNull(sepayProperties.secretKey());
        boolean apiKeyConfigured = configuredApiKey != null;
        boolean secretKeyConfigured = configuredSecretKey != null;

        if (!apiKeyConfigured && !secretKeyConfigured) {
            return;
        }

        boolean apiKeyMatched = apiKeyConfigured
                && ("Apikey " + configuredApiKey).equals(command.authorizationHeader());
        boolean secretKeyMatched = secretKeyConfigured
                && configuredSecretKey.equals(command.secretKeyHeader());

        if (!apiKeyMatched && !secretKeyMatched) {
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

    private Optional<Payment> resolvePendingPayment(HandleSepayIpnCommand command) {
        String code = command.code();
        if (code != null) {
            Optional<Payment> byCode = paymentRepository.findPendingSepayByOrderCode(code);
            if (byCode.isPresent()) {
                return byCode;
            }
        }

        String content = command.content();
        if (content == null) {
            return Optional.empty();
        }

        return paymentRepository.findPendingSepayByTransferContentInContent(content);
    }

    private String resolveMerchantId(Payment payment) {
        String configuredMerchantId = StringUtils.trimToNull(sepayProperties.merchantId());
        if (configuredMerchantId != null) {
            return configuredMerchantId;
        }
        return payment.getMerchantId();
    }
}
