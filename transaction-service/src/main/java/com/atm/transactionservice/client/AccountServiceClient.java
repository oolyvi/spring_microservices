package com.atm.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;

@FeignClient(name = "account-service")
public interface AccountServiceClient {

    record AccountResponse(
            Long id,
            BigDecimal balance
    ) {
    }

    record AmountRequest(
            BigDecimal amount,
            String idempotencyKey
    ) {
    }

    @GetMapping("/account/{id}")
    AccountResponse getAccount(@PathVariable("id") Long accountId);

    @PostMapping("/account/{id}/debit")
    void debitInternal(
            @PathVariable("id") Long accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody AmountRequest request
    );

    default void debit(
            Long accountId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        debitInternal(accountId, idempotencyKey, new AmountRequest(amount, idempotencyKey));
    }

    @PostMapping("/account/{id}/credit")
    void creditInternal(
            @PathVariable("id") Long accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody AmountRequest request
    );

    default void credit(
            Long accountId,
            BigDecimal amount,
            String idempotencyKey
    ) {
        creditInternal(accountId, idempotencyKey, new AmountRequest(amount, idempotencyKey));
    }
}