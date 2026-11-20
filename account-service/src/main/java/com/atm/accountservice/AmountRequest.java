package com.atm.accountservice;

import java.math.BigDecimal;

public record AmountRequest(BigDecimal amount, String idempotencyKey) {
}