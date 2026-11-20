package com.atm.cardservice;

public record CreateCardRequest(
        String cardNumber,
        String pin,
        Card.CardType cardType,
        Long customerId,
        Long accountId
) {
}