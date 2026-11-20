package com.atm.transactionservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionReceiptDto(
        Long transactionId,
        String idempotencyKey,
        Long accountId,
        String type,
        BigDecimal amount,
        LocalDateTime timestamp,
        BigDecimal currentAccountBalance
) {
}