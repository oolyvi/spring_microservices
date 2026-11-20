package com.atm.transactionservice.event;

import java.math.BigDecimal;

public record TransactionCompletedEvent(
        Long transactionId,
        String type,
        BigDecimal amount,
        Long accountId,
        Long relatedAccountId,
        Long originalTransactionId,
        String occurredAt
) {
}