package com.atm.notificationservice.event;

public record CashLowEvent(
        Long atmId,
        Integer denomination,
        Integer remaining,
        Integer threshold,
        String occurredAt
) {
}