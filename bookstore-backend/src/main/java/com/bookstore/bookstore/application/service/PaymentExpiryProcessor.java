package com.bookstore.bookstore.application.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentExpiryProcessor {

    private final OrderCancellationService orderCancellationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean expireOne(UUID paymentId, Instant now) {
        return orderCancellationService.expirePendingPayment(paymentId, now);
    }
}
