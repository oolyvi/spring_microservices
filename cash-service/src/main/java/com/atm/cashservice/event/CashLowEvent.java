package com.atm.cashservice.event;

public record CashLowEvent(
        Long atmId,
        Integer denomination,
        Integer remaining,
        Integer threshold,
        String occurredAt
) {
}