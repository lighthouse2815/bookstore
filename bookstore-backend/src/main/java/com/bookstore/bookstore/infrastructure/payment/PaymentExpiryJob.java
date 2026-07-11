package com.bookstore.bookstore.infrastructure.payment;

import com.bookstore.bookstore.application.port.out.IPaymentExpirySettings;
import com.bookstore.bookstore.application.port.out.IPaymentRepository;
import com.bookstore.bookstore.application.service.PaymentExpiryProcessor;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExpiryJob {

    private final IPaymentRepository paymentRepository;
    private final IPaymentExpirySettings paymentExpirySettings;
    private final PaymentExpiryProcessor paymentExpiryProcessor;

    @Scheduled(fixedDelayString = "${app.payment.expiry-job-delay-ms}")
    public void expirePendingPayments() {
        if (!paymentExpirySettings.expiryJobEnabled()) {
            return;
        }

        Instant now = Instant.now();
        paymentRepository.findPendingExpiredIds(now, paymentExpirySettings.expiryJobBatchSize())
                .forEach(paymentId -> {
                    try {
                        paymentExpiryProcessor.expireOne(paymentId, now);
                    } catch (RuntimeException exception) {
                        log.error("Unable to expire pending payment paymentId={}", paymentId, exception);
                    }
                });
    }
}
