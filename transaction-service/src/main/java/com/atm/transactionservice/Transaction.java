package com.atm.transactionservice;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transaction",
        indexes = {
                @Index(name = "idx_transaction_account", columnList = "account_id"),
                @Index(name = "idx_transaction_timestamp", columnList = "timestamp")
        }
)
public class Transaction {

    public enum TransactionType {
        DEBIT,
        CREDIT,
        TRANSFER
    }

    public enum TransactionStatus {
        COMPLETED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key",
            unique = true, nullable = false, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "related_account_id")
    private Long relatedAccountId;

    @Column(name = "original_transaction_id")
    private Long originalTransactionId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public Transaction() {
    }

    public Transaction(
            String idempotencyKey,
            TransactionType type,
            BigDecimal amount,
            Long accountId,
            Long relatedAccountId,
            Long originalTransactionId
    ) {
        this.idempotencyKey = idempotencyKey;
        this.type = type;
        this.status = TransactionStatus.COMPLETED;
        this.amount = amount;
        this.accountId = accountId;
        this.relatedAccountId = relatedAccountId;
        this.originalTransactionId = originalTransactionId;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getRelatedAccountId() {
        return relatedAccountId;
    }

    public Long getOriginalTransactionId() {
        return originalTransactionId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}