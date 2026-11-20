package com.atm.transactionservice;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/execute")
    public ResponseEntity<TransactionResponseDto> executeTransaction(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransactionRequest request
    ) {
        /*
         http://localhost:8088/transaction/execute

         on body:
         {
          "accountId": 1,
          "amount": 150.00,
          "type": "DEBIT"
         }

         on header:
         Idempotency-Key: {{$guid}}  for dynamic keys that one key can handle multiple requests
         */
        Transaction transaction = transactionService.processTransaction(
                idempotencyKey,
                request
        );

        System.out.println("[ATM_DEBUG]: " + request.amount() + " is removed from for id: " + request.accountId());

        return ResponseEntity.ok(
                TransactionResponseDto.from(transaction)
        );
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionHistory(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,
            Pageable pageable
    ) {
        /*
         http://localhost:8088/transaction?accountId=1&startDate=2026-01-01T00:00:00&page=0&size=10
         */
        Page<TransactionResponseDto> transactions =
                transactionService.getTransactions(
                        accountId,
                        startDate,
                        endDate,
                        pageable
                ).map(TransactionResponseDto::from);

        System.out.println("[ATM_DEBUG]: transaction history got from account_id: [" + accountId + "] starts from: [" + startDate + "] to end date: [" + endDate + "]");

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(
            @PathVariable Long id
    ) {
        /*
        http://localhost:8088/transaction/1
         */
        Transaction transaction =
                transactionService.getTransactionById(id);

        return ResponseEntity.ok(
                TransactionResponseDto.from(transaction)
        );
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<TransactionResponseDto> refundTransaction(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable Long id
    ) {
        /*
        http://localhost:8088/transaction/1/refund

        header:
        exact Idempotency-Key for the refund (not the original transaction key)
         */
        Transaction transaction =
                transactionService.refundTransaction(
                        idempotencyKey,
                        id
                );

        System.out.println("[ATM_DEBUG]: refund requested for id: " + id);

        return ResponseEntity.ok(
                TransactionResponseDto.from(transaction)
        );
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<TransactionReceiptDto>
    getTransactionReceipt(@PathVariable Long id) {
        /*
         http://localhost:8088/transaction/1/receipt
         */
        System.out.println("[ATM_DEBUG]: got receipt for id: " + id);

        return ResponseEntity.ok(
                transactionService.generateReceipt(id)
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDto> transferFunds(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request
    ) {
        /*
         http://localhost:8088/transaction/transfer

         body:
         {
          "sourceAccountId": 1,
          "destinationAccountId": 2,
          "amount": 50.00
         }

         header:
         Idempotency-Key: {{$guid}}
         */
        Transaction transaction =
                transactionService.transferMoney(
                        idempotencyKey,
                        request
                );

        System.out.println("[ATM_DEBUG]: " + request.amount() + " transferred from account " + request.sourceAccountId() + " to " + request.destinationAccountId());

        return ResponseEntity.ok(
                TransactionResponseDto.from(transaction)
        );
    }
}