package com.atm.cardservice;

import jakarta.persistence.*;

@Entity
@Table(name = "card")
public class Card {

    public enum CardType {
        DEBIT,
        CREDIT
    }

    public enum CardStatus {
        ACTIVE,
        BLOCKED,
        EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true, length = 30)
    private String cardNumber;

    @Column(name = "pin_hash", nullable = false, length = 255)
    private String pinHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 20)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    public Card() {
    }

    public Card(String cardNumber, String pinHash, CardType cardType,
                Long customerId, Long accountId) {
        this.cardNumber = cardNumber;
        this.pinHash = pinHash;
        this.cardType = cardType;
        this.customerId = customerId;
        this.accountId = accountId;
    }

    public static String maskCardNumber(String rawCardNumber) {
        if (rawCardNumber == null || rawCardNumber.length() < 4) {
            return "****";
        }
        String last4 = rawCardNumber.substring(rawCardNumber.length() - 4);
        return "****-****-****-" + last4;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}