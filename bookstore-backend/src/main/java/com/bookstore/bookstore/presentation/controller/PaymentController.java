package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.command.HandleSepayIpnCommand;
import com.bookstore.bookstore.application.port.in.IPaymentService;
import com.bookstore.bookstore.presentation.request.SepayWebhookRequest;
import com.bookstore.bookstore.presentation.response.SepayWebhookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/api/payments/sepay/ipn")
    public ResponseEntity<SepayWebhookResponse> handleSepayIpn(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestHeader(value = "X-Secret-Key", required = false) String secretKeyHeader,
            @RequestBody SepayWebhookRequest request
    ) {
        paymentService.handleSepayIpn(new HandleSepayIpnCommand(
                authorizationHeader,
                secretKeyHeader,
                request.resolvedTransactionId(),
                request.gateway(),
                request.transactionDate(),
                request.accountNumber(),
                request.subAccount(),
                request.code(),
                request.content(),
                request.transferType(),
                request.description(),
                request.transferAmount(),
                request.referenceCode(),
                request.accumulated()
        ));
        return ResponseEntity.ok(new SepayWebhookResponse(true));
    }
}
