package com.atm.cardservice;

import com.atm.cardservice.client.AccountInterface;
import com.atm.cardservice.client.CustomerInterface;
import com.atm.cardservice.event.CardEvent;
import com.atm.cardservice.event.CardEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CustomerInterface customerInterface;
    private final AccountInterface accountInterface;
    private final PasswordEncoder passwordEncoder;
    private final CardEventPublisher eventPublisher;

    public CardService(
            CardRepository cardRepository,
            CustomerInterface customerInterface,
            AccountInterface accountInterface,
            PasswordEncoder passwordEncoder,
            CardEventPublisher eventPublisher) {
        this.cardRepository = cardRepository;
        this.customerInterface = customerInterface;
        this.accountInterface = accountInterface;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CardDto addCard(
            String rawCardNumber,
            String rawPin,
            Card.CardType cardType,
            Long customerId,
            Long accountId
    ) {
        customerInterface.verifyCustomer(customerId);
        accountInterface.verifyAccount(accountId);

        String hashedPin = passwordEncoder.encode(rawPin);

        Card card = new Card(
                rawCardNumber.trim(),
                hashedPin,
                cardType,
                customerId,
                accountId
        );

        Card savedCard = cardRepository.save(card);

        return CardDto.fromEntity(savedCard);
    }

    public boolean verifyPin(Long cardId, String rawPin) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException());

        return passwordEncoder.matches(rawPin, card.getPinHash());
    }

    @Transactional(readOnly = true)
    public List<CardDto> getAllCards() {
        return cardRepository.findAll().stream().map(CardDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CardDto getCardById(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException());
        return CardDto.fromEntity(card);
    }

    @Transactional(readOnly = true)
    public void authenticateCard(String rawCardNumber, String rawPin) {

        Card card = cardRepository.findByCardNumber(rawCardNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid card or PIN"
                ));

        if (card.getStatus() != Card.CardStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Card is not active"
            );
        }

        if (!passwordEncoder.matches(rawPin, card.getPinHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid card or PIN"
            );
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalanceByCardNumber(String rawCardNumber) {

        if (rawCardNumber == null || rawCardNumber.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Card number is required"
            );
        }

        String cardNumber = rawCardNumber.trim();

        Card card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Card not found"
                ));

        if (card.getStatus() != Card.CardStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Card is not active"
            );
        }

        return accountInterface.getAccountBalance(card.getAccountId());
    }

    @Transactional
    public void changePin(Long cardId, String oldPin, String newPin) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException());

        if (!passwordEncoder.matches(oldPin, card.getPinHash())) {
            throw new SecurityException("Invalid current PIN");
        }

        if (newPin == null || !newPin.matches("\\d{4}")) {
            throw new IllegalArgumentException("New PIN must be a 4-digit number");
        }

        if (oldPin.equals(newPin)) {
            throw new IllegalArgumentException("New PIN cannot be the same as the old PIN");
        }

        card.setPinHash(passwordEncoder.encode(newPin));
        cardRepository.save(card);
        eventPublisher.publish(card, CardEvent.PIN_CHANGED);
    }

    @Transactional
    public void blockCard(Long cardId) {
        Card card = findCard(cardId);
        card.setStatus(Card.CardStatus.BLOCKED);
        cardRepository.save(card);
        eventPublisher.publish(card, CardEvent.BLOCKED);
    }

    @Transactional
    public void unblockCard(Long cardId) {
        Card card = findCard(cardId);
        card.setStatus(Card.CardStatus.ACTIVE);
        cardRepository.save(card);
        eventPublisher.publish(card, CardEvent.UNBLOCKED);
    }

    @Transactional
    public void forceResetPin(Long cardId, String temporaryPinHash) {
        if (temporaryPinHash == null || !temporaryPinHash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$")) {
            throw new IllegalArgumentException("temporaryPinHash must be a BCrypt hash");
        }
        Card card = findCard(cardId);
        card.setPinHash(temporaryPinHash);
        cardRepository.save(card);
        eventPublisher.publish(card, CardEvent.PIN_RESET);
    }

    private Card findCard(Long cardId) {
        return cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException());
    }
}