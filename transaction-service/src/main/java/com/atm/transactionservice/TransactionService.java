package com.atm.transactionservice;

import com.atm.transactionservice.client.AccountServiceClient;
import com.atm.transactionservice.event.TransactionEventPublisher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionEventPublisher eventPublisher;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountServiceClient accountServiceClient,
            TransactionEventPublisher eventPublisher
    ) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
        this.eventPublisher = eventPublisher;
    }

    public Transaction processTransaction(
            String idempotencyKey,
            TransactionRequest request
    ) {

        var existing = transactionRepository
                .findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            return existing.get();
        }

        validateAmount(request.amount());

        if (request.type() == Transaction.TransactionType.TRANSFER) {
            throw new IllegalArgumentException(
                    "Use the transfer endpoint for transfers"
            );
        }

        if (request.type() == Transaction.TransactionType.DEBIT) {
            accountServiceClient.debit(
                    request.accountId(),
                    request.amount(),
                    idempotencyKey
            );

        } else if (request.type() == Transaction.TransactionType.CREDIT) {
            accountServiceClient.credit(
                    request.accountId(),
                    request.amount(),
                    idempotencyKey
            );
        }

        Transaction transaction = new Transaction(
                idempotencyKey,
                request.type(),
                request.amount(),
                request.accountId(),
                null,
                null
        );

        return saveAndPublish(transaction);
    }

    public Page<Transaction> getTransactions(
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {

        return transactionRepository.findTransactions(
                accountId,
                startDate,
                endDate,
                pageable
        );
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() ->
                        new TransactionNotFoundException(id)
                );
    }

    public Transaction refundTransaction(
            String idempotencyKey,
            Long originalTransactionId
    ) {
        var existing = transactionRepository
                .findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            return existing.get();
        }

        Transaction original = getTransactionById(originalTransactionId);

        if (transactionRepository.existsByOriginalTransactionId(
                originalTransactionId
        )) {
            throw new IllegalStateException(
                    "Transaction has already been refunded"
            );
        }

        Long accountId = original.getAccountId();

        switch (original.getType()) {
            case DEBIT -> accountServiceClient.credit(
                    accountId,
                    original.getAmount(),
                    idempotencyKey
            );
            case CREDIT -> accountServiceClient.debit(
                    accountId,
                    original.getAmount(),
                    idempotencyKey
            );
            case TRANSFER -> {
                Long destinationId = original.getRelatedAccountId();

                accountServiceClient.debit(
                        destinationId,
                        original.getAmount(),
                        idempotencyKey + "-DEBIT"
                );

                try {
                    accountServiceClient.credit(
                            accountId,
                            original.getAmount(),
                            idempotencyKey + "-CREDIT"
                    );
                } catch (Exception exception) {
                    accountServiceClient.credit(
                            destinationId,
                            original.getAmount(),
                            idempotencyKey + "-COMPENSATION"
                    );

                    throw exception;
                }
            }
        }

        Transaction refund = new Transaction(
                idempotencyKey,
                original.getType() == Transaction.TransactionType.DEBIT
                        ? Transaction.TransactionType.CREDIT
                        : original.getType() == Transaction.TransactionType.CREDIT
                        ? Transaction.TransactionType.DEBIT
                        : Transaction.TransactionType.TRANSFER,
                original.getAmount(),
                original.getType() == Transaction.TransactionType.TRANSFER
                        ? original.getRelatedAccountId()
                        : accountId,
                original.getType() == Transaction.TransactionType.TRANSFER
                        ? accountId
                        : null,
                originalTransactionId
        );

        return saveAndPublish(refund);
    }

    public TransactionReceiptDto generateReceipt(Long transactionId) {

        Transaction transaction = getTransactionById(transactionId);

        var account = accountServiceClient.getAccount(
                transaction.getAccountId()
        );

        return new TransactionReceiptDto(
                transaction.getId(),
                transaction.getIdempotencyKey(),
                transaction.getAccountId(),
                transaction.getType().name(),
                transaction.getAmount(),
                transaction.getTimestamp(),
                account.balance()
        );
    }

    public Transaction transferMoney(
            String idempotencyKey,
            TransferRequest request
    ) {

        var existing = transactionRepository
                .findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            return existing.get();
        }

        validateAmount(request.amount());

        Long sourceId = request.sourceAccountId();
        Long destinationId = request.destinationAccountId();

        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts cannot be the same"
            );
        }

        accountServiceClient.debit(
                sourceId,
                request.amount(),
                idempotencyKey + "-DEBIT"
        );

        try {
            accountServiceClient.credit(
                    destinationId,
                    request.amount(),
                    idempotencyKey + "-CREDIT"
            );

        } catch (Exception exception) {
            accountServiceClient.credit(
                    sourceId,
                    request.amount(),
                    idempotencyKey + "-COMPENSATION"
            );

            throw exception;
        }

        Transaction transaction = new Transaction(
                idempotencyKey,
                Transaction.TransactionType.TRANSFER,
                request.amount(),
                sourceId,
                destinationId,
                null
        );

        return saveAndPublish(transaction);
    }

    private Transaction saveAndPublish(Transaction transaction) {
        Transaction saved = transactionRepository.save(transaction);
        eventPublisher.publishCompleted(saved);
        return saved;
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }
    }
}