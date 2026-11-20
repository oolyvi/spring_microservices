package com.atm.cashservice;

import com.atm.cashservice.event.CashEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private CashRepository cashRepository;

    @Mock
    private CashEventPublisher eventPublisher;

    @InjectMocks
    private CashService cashService;

    private Cash cash200;
    private Cash cash100;
    private Cash cash50;

    @BeforeEach
    void setUp() {
        cash200 = new Cash(1L, 200, 2, 1);
        cash100 = new Cash(1L, 100, 3, 1);
        cash50 = new Cash(1L, 50, 4, 1);
    }

    @Test
    void withdrawCash_Success() {
        when(cashRepository.findByAtmIdOrderByDenominationDesc(1L)).thenReturn(List.of(cash200, cash100, cash50));

        Map<Integer, Integer> breakdown = cashService.withdrawCash(1L, 350);

        assertEquals(3, breakdown.size());
        assertEquals(1, breakdown.get(200));
        assertEquals(1, breakdown.get(100));
        assertEquals(1, breakdown.get(50));
        assertEquals(1, cash200.getQuantity());
        assertEquals(2, cash100.getQuantity());
        assertEquals(3, cash50.getQuantity());
        verify(cashRepository, times(3)).save(any(Cash.class));
    }

    @Test
    void withdrawCash_UsesSmallerDenominationsWhenNeeded() {
        when(cashRepository.findByAtmIdOrderByDenominationDesc(1L)).thenReturn(List.of(cash200, cash100, cash50));

        Map<Integer, Integer> breakdown = cashService.withdrawCash(1L, 150);

        assertEquals(2, breakdown.size());
        assertEquals(1, breakdown.get(100));
        assertEquals(1, breakdown.get(50));

        assertEquals(2, cash200.getQuantity());
        assertEquals(2, cash100.getQuantity());
        assertEquals(3, cash50.getQuantity());
    }

    @Test
    void withdrawCash_ZeroOrNegativeAmount_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> cashService.withdrawCash(1L, 0));

        assertEquals("Withdrawal amount must be greater than zero.", exception.getMessage());
        verifyNoInteractions(cashRepository);
    }

    @Test
    void withdrawCash_NoInventory_ThrowsException() {
        when(cashRepository.findByAtmIdOrderByDenominationDesc(99L)).thenReturn(List.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> cashService.withdrawCash(99L, 100));

        assertTrue(exception.getMessage().contains("No cash inventory found"));
        verify(cashRepository, never()).save(any(Cash.class));
    }

    @Test
    void withdrawCash_InsufficientOrNonExactCash_ThrowsExceptionWithoutUpdatingInventory() {
        Cash onlyOnes = new Cash(1L, 1, 2, 1);
        when(cashRepository.findByAtmIdOrderByDenominationDesc(1L)).thenReturn(List.of(onlyOnes));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> cashService.withdrawCash(1L, 50));

        assertTrue(exception.getMessage().contains("Unable to dispense exact amount"));
        assertEquals(2, onlyOnes.getQuantity());
        verify(cashRepository, never()).save(any(Cash.class));
    }

    @Test
    void withdrawCash_UpdatesAllCassetteQuantities() {
        when(cashRepository.findByAtmIdOrderByDenominationDesc(1L)).thenReturn(List.of(cash200, cash100, cash50));

        Map<Integer, Integer> breakdown = cashService.withdrawCash(1L, 500);

        assertEquals(Map.of(200, 2, 100, 1), breakdown);
        assertEquals(0, cash200.getQuantity());
        assertEquals(2, cash100.getQuantity());
        assertEquals(4, cash50.getQuantity());
        verify(cashRepository, times(3)).save(any(Cash.class));
    }

    @Test
    void withdrawCash_publishesLowCashEventOnlyForCassettesThatHitTheThreshold() {
        when(cashRepository.findByAtmIdOrderByDenominationDesc(1L)).thenReturn(List.of(cash200, cash100, cash50));

        cashService.withdrawCash(1L, 350);

        // 200s: 2 -> 1 (threshold 1) => alert. 100s: 3 -> 2 and 50s: 4 -> 3 are above the threshold.
        verify(eventPublisher).publishCashLow(1L, 200, 1, 1);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    void withdrawCash_doesNotAlertForCassettesThatWereNotUsed() {
        Cash emptyTens = new Cash(1L, 10, 0, 5);
        when(cashRepository.findByAtmIdOrderByDenominationDesc(1L)).thenReturn(List.of(cash200, emptyTens));

        cashService.withdrawCash(1L, 200);

        verify(eventPublisher, never()).publishCashLow(eq(1L), eq(10), anyInt(), anyInt());
    }
}