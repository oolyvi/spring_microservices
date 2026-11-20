package com.atm.transactionservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDto(
        Long id,
        String idempotencyKey,
        Transaction.TransactionType type,
        Transaction.TransactionStatus status,
        BigDecimal amount,
        Long accountId,
        Long relatedAccountId,
        Long originalTransactionId,
        LocalDateTime timestamp

) {
    public static TransactionResponseDto from(Transaction transaction) {

        return new TransactionResponseDto(
                transaction.getId(),
                transaction.getIdempotencyKey(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getAccountId(),
                transaction.getRelatedAccountId(),
                transaction.getOriginalTransactionId(),
                transaction.getTimestamp()
        );
    }
}