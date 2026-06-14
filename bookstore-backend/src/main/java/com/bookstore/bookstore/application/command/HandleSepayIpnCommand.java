package com.bookstore.bookstore.application.command;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;

public record HandleSepayIpnCommand(
        String authorizationHeader,
        String secretKeyHeader,
        String transactionId,
        String gateway,
        String transactionDate,
        String accountNumber,
        String subAccount,
        String code,
        String content,
        String transferType,
        String description,
        BigDecimal transferAmount,
        String referenceCode,
        BigDecimal accumulated
) {
    public HandleSepayIpnCommand {
        if (transferAmount != null && transferAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "transferAmount");
        }
        authorizationHeader = StringUtils.trimToNull(authorizationHeader);
        secretKeyHeader = StringUtils.trimToNull(secretKeyHeader);
        transactionId = StringUtils.trimToNull(transactionId);
        gateway = StringUtils.trimToNull(gateway);
        transactionDate = StringUtils.trimToNull(transactionDate);
        accountNumber = StringUtils.trimToNull(accountNumber);
        subAccount = StringUtils.trimToNull(subAccount);
        code = StringUtils.trimToNull(code);
        content = StringUtils.trimToNull(content);
        transferType = StringUtils.trimToNull(transferType);
        description = StringUtils.trimToNull(description);
        referenceCode = StringUtils.trimToNull(referenceCode);
    }
}
