package com.bookstore.bookstore.presentation.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;

public record SepayWebhookRequest(
        @JsonAlias({"id"})
        Long id,

        @JsonAlias({"transactionId", "transaction_id"})
        String transactionId,

        @JsonAlias({"gateway"})
        String gateway,

        @JsonAlias({"transactionDate", "transaction_date"})
        String transactionDate,

        @JsonAlias({"accountNumber", "account_number"})
        String accountNumber,

        @JsonAlias({"subAccount", "sub_account", "va"})
        String subAccount,

        @JsonAlias({"code", "payment_code"})
        String code,

        @JsonAlias({"content", "transferContent", "transfer_content"})
        String content,

        @JsonAlias({"transferType", "transfer_type"})
        String transferType,

        @JsonAlias({"description"})
        String description,

        @JsonAlias({"transferAmount", "amount"})
        BigDecimal transferAmount,

        @JsonAlias({"referenceCode", "reference_code"})
        String referenceCode,

        @JsonAlias({"accumulated"})
        BigDecimal accumulated
) {
    public String resolvedTransactionId() {
        if (transactionId != null && !transactionId.isBlank()) {
            return transactionId;
        }
        return id == null ? null : String.valueOf(id);
    }
}
