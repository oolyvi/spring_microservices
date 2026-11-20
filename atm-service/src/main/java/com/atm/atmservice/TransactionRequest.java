package com.atm.atmservice;

import java.math.BigDecimal;

public record TransactionRequest(
        Long accountId,
        BigDecimal amount,
        String type) {
}