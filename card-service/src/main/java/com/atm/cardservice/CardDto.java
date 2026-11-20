package com.atm.cardservice;

public record CardDto(
        Long id,
        String maskedCardNumber,
        Card.CardType cardType,
        Card.CardStatus status,
        Long customerId,
        Long accountId
) {
    public static CardDto fromEntity(Card card) {
        if (card == null) return null;

        return new CardDto(
                card.getId(),
                Card.maskCardNumber(card.getCardNumber()),
                card.getCardType(),
                card.getStatus(),
                card.getCustomerId(),
                card.getAccountId()
        );
    }
}