package com.bookstore.bookstore.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bookstore.bookstore.domain.enums.PaymentProvider;
import com.bookstore.bookstore.domain.enums.PaymentStatus;
import com.bookstore.bookstore.infrastructure.persistence.entity.PaymentJpaEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PaymentJpaRepositoryTest {

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Test
    void findPendingSepayByOrderCode_filtersByProviderAndStatusForBothQueries() {
        PaymentJpaEntity expectedPayment = payment(
                PaymentProvider.SEPAY,
                PaymentStatus.PENDING,
                "DH123",
                "DH123",
                Instant.parse("2026-01-01T08:00:00Z")
        );
        PaymentJpaEntity paidPayment = payment(
                PaymentProvider.SEPAY,
                PaymentStatus.PAID,
                "DH123",
                "DH123",
                Instant.parse("2026-01-01T08:01:00Z")
        );
        PaymentJpaEntity wrongProviderPayment = payment(
                PaymentProvider.COD,
                PaymentStatus.PENDING,
                "DH123",
                "DH123",
                Instant.parse("2026-01-01T08:02:00Z")
        );
        paymentJpaRepository.saveAll(List.of(expectedPayment, paidPayment, wrongProviderPayment));

        assertEquals(
                expectedPayment.getId(),
                paymentJpaRepository.findPendingSepayByOrderCode(
                                "DH123",
                                PaymentProvider.SEPAY,
                                PaymentStatus.PENDING
                        )
                        .orElseThrow()
                        .getId()
        );
        assertEquals(
                expectedPayment.getId(),
                paymentJpaRepository.findPendingSepayByOrderCodeForUpdate(
                                "DH123",
                                PaymentProvider.SEPAY,
                                PaymentStatus.PENDING
                        )
                        .orElseThrow()
                        .getId()
        );
    }

    @Test
    void findPendingSepayByTransferContentInContent_matchesCaseInsensitivelyForBothQueries() {
        PaymentJpaEntity expectedPayment = payment(
                PaymentProvider.SEPAY,
                PaymentStatus.PENDING,
                "DH456",
                "DH456",
                Instant.parse("2026-01-01T09:00:00Z")
        );
        PaymentJpaEntity wrongStatusPayment = payment(
                PaymentProvider.SEPAY,
                PaymentStatus.PAID,
                "DH456",
                "DH456",
                Instant.parse("2026-01-01T09:01:00Z")
        );
        paymentJpaRepository.saveAll(List.of(expectedPayment, wrongStatusPayment));

        assertEquals(
                expectedPayment.getId(),
                paymentJpaRepository.findPendingSepayByTransferContentInContent(
                                "khach chuyen khoan dh456 luc 9h",
                                PaymentProvider.SEPAY,
                                PaymentStatus.PENDING
                        )
                        .orElseThrow()
                        .getId()
        );
        assertEquals(
                expectedPayment.getId(),
                paymentJpaRepository.findPendingSepayByTransferContentInContentForUpdate(
                                "khach chuyen khoan dh456 luc 9h",
                                PaymentProvider.SEPAY,
                                PaymentStatus.PENDING
                        )
                        .orElseThrow()
                        .getId()
        );
    }

    private static PaymentJpaEntity payment(
            PaymentProvider provider,
            PaymentStatus status,
            String referenceCode,
            String transferContent,
            Instant createdAt
    ) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrderId(UUID.randomUUID());
        entity.setProvider(provider);
        entity.setStatus(status);
        entity.setAmount(new BigDecimal("100000.00"));
        entity.setMerchantId("merchant-123");
        entity.setTransactionId(UUID.randomUUID().toString());
        entity.setReferenceCode(referenceCode);
        entity.setTransferContent(transferContent);
        entity.setGateway("SEPAY");
        entity.setPaidAt(status == PaymentStatus.PAID ? createdAt.plusSeconds(60) : null);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(createdAt);
        return entity;
    }
}
