package com.atm.cardservice;

import com.atm.cardservice.client.AccountInterface;
import com.atm.cardservice.client.CustomerInterface;
import com.atm.cardservice.event.CardEvent;
import com.atm.cardservice.event.CardEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountInterface accountInterface;

    @Mock
    private CustomerInterface customerInterface;

    @Mock
    private CardEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CardService cardService;

    private Card card;

    private final BCryptPasswordEncoder realEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        lenient().when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> realEncoder.encode(invocation.getArgument(0)));

        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenAnswer(invocation -> realEncoder.matches(invocation.getArgument(0), invocation.getArgument(1)));

        card = new Card();
        card.setId(1L);
        card.setCardNumber("1234567812345678");
        card.setPinHash(passwordEncoder.encode("1234"));
        card.setCardType(Card.CardType.DEBIT);
        card.setCustomerId(1L);
        card.setAccountId(100L);
        card.setStatus(Card.CardStatus.ACTIVE);
    }

    @Test
    void getCardById_returnsCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        CardDto result = cardService.getCardById(1L);

        assertEquals(1L, result.id());
        assertEquals("****-****-****-5678", result.maskedCardNumber());
        assertEquals(Card.CardType.DEBIT, result.cardType());
        verify(cardRepository).findById(1L);
    }

    @Test
    void getCardById_throwsWhenCardDoesNotExist() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cardService.getCardById(99L));

        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    void getAllCards_returnsDtos() {
        when(cardRepository.findAll()).thenReturn(List.of(card));

        List<CardDto> result = cardService.getAllCards();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().id());
    }

    @Test
    void verifyPin_returnsTrueForCorrectPin() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertTrue(cardService.verifyPin(1L, "1234"));
    }

    @Test
    void verifyPin_returnsFalseForIncorrectPin() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertFalse(cardService.verifyPin(1L, "9999"));
    }

    @Test
    void verifyPin_throwsWhenCardDoesNotExist() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cardService.verifyPin(99L, "1234"));
    }

    @Test
    void authenticateCard_succeedsForActiveCardAndCorrectPin() {
        when(cardRepository.findByCardNumber("1234567812345678")).thenReturn(Optional.of(card));

        assertDoesNotThrow(() -> cardService.authenticateCard("1234567812345678", "1234"));
    }

    @Test
    void authenticateCard_throwsForIncorrectPin() {
        when(cardRepository.findByCardNumber("1234567812345678")).thenReturn(Optional.of(card));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardService.authenticateCard("1234567812345678", "0000"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void authenticateCard_throwsForBlockedCard() {
        card.setStatus(Card.CardStatus.BLOCKED);
        when(cardRepository.findByCardNumber("1234567812345678")).thenReturn(Optional.of(card));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cardService.authenticateCard("1234567812345678", "1234"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getBalanceByCardNumber_returnsAccountBalance() {
        when(cardRepository.findByCardNumber("1234567812345678")).thenReturn(Optional.of(card));
        when(accountInterface.getAccountBalance(100L)).thenReturn(new BigDecimal("250.00"));

        assertEquals(new BigDecimal("250.00"), cardService.getBalanceByCardNumber("1234567812345678"));
    }

    @Test
    void changePin_updatesPinHash() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.changePin(1L, "1234", "5678");

        verify(passwordEncoder).encode("5678");
        verify(cardRepository).save(card);
        verify(eventPublisher).publish(card, CardEvent.PIN_CHANGED);
    }

    @Test
    void changePin_throwsForWrongOldPin() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(SecurityException.class, () -> cardService.changePin(1L, "0000", "5678"));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void changePin_rejectsInvalidNewPin() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(IllegalArgumentException.class, () -> cardService.changePin(1L, "1234", "12ab"));
    }

    @Test
    void changePin_rejectsSamePin() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(IllegalArgumentException.class, () -> cardService.changePin(1L, "1234", "1234"));
    }

    @Test
    void blockCard_setsStatusBlockedAndPublishesEvent() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.blockCard(1L);

        assertEquals(Card.CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
        verify(eventPublisher).publish(card, CardEvent.BLOCKED);
    }

    @Test
    void unblockCard_setsStatusActiveAndPublishesEvent() {
        card.setStatus(Card.CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.unblockCard(1L);

        assertEquals(Card.CardStatus.ACTIVE, card.getStatus());
        verify(eventPublisher).publish(card, CardEvent.UNBLOCKED);
    }

    @Test
    void blockCard_throwsWhenCardDoesNotExist() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.blockCard(99L));
        verify(eventPublisher, never()).publish(any(), anyString());
    }

    @Test
    void forceResetPin_storesSuppliedBcryptHash() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        String hash = realEncoder.encode("4321");

        cardService.forceResetPin(1L, hash);

        assertEquals(hash, card.getPinHash());
        verify(eventPublisher).publish(card, CardEvent.PIN_RESET);
    }

    @Test
    void forceResetPin_rejectsValueThatIsNotABcryptHash() {
        assertThrows(IllegalArgumentException.class, () -> cardService.forceResetPin(1L, "1234"));
        verify(cardRepository, never()).save(any());
    }
}