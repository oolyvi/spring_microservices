package com.atm.cardservice.event;

public record CardEvent(
        Long cardId,
        String maskedCardNumber,
        Long customerId,
        Long accountId,
        String eventType,
        String occurredAt
) {
    public static final String CREATED = "CREATED";
    public static final String BLOCKED = "BLOCKED";
    public static final String UNBLOCKED = "UNBLOCKED";
    public static final String PIN_CHANGED = "PIN_CHANGED";
    public static final String PIN_RESET = "PIN_RESET";
}