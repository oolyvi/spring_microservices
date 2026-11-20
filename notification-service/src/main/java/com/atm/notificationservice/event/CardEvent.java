package com.atm.notificationservice.event;

public record CardEvent(
        Long cardId,
        String maskedCardNumber,
        Long customerId,
        Long accountId,
        String eventType,
        String occurredAt
) {
}