package com.atm.transactionservice;

import com.atm.transactionservice.client.AccountServiceClient;
import com.atm.transactionservice.event.TransactionEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private TransactionEventPublisher eventPublisher;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequest debitRequest;

    @BeforeEach
    void setUp() {
        debitRequest = new TransactionRequest(
                1L, new BigDecimal("100.00"), Transaction.TransactionType.DEBIT
        );
    }

    @Test
    void processTransaction_Debit_Success() {
        when(transactionRepository.findByIdempotencyKey("key-123"))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.processTransaction("key-123", debitRequest);

        assertNotNull(result);
        assertEquals(Transaction.TransactionType.DEBIT, result.getType());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(1L, result.getAccountId());
        verify(accountServiceClient).debit(1L, new BigDecimal("100.00"), "key-123");
        verify(transactionRepository).save(any(Transaction.class));
        verify(eventPublisher).publishCompleted(result);
    }

    @Test
    void processTransaction_Credit_Success() {
        when(transactionRepository.findByIdempotencyKey("credit-key"))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.processTransaction(
                "credit-key",
                new TransactionRequest(1L, new BigDecimal("25.00"),
                        Transaction.TransactionType.CREDIT)
        );

        assertEquals(Transaction.TransactionType.CREDIT, result.getType());
        verify(accountServiceClient).credit(1L, new BigDecimal("25.00"), "credit-key");
    }

    @Test
    void processTransaction_IdempotentRequest_ReturnsExisting() {
        Transaction existing = new Transaction(
                "key-123", Transaction.TransactionType.CREDIT,
                new BigDecimal("50.00"), 1L, null, null
        );
        when(transactionRepository.findByIdempotencyKey("key-123"))
                .thenReturn(Optional.of(existing));

        Transaction result = transactionService.processTransaction(
                "key-123",
                new TransactionRequest(1L, new BigDecimal("50.00"),
                        Transaction.TransactionType.CREDIT)
        );

        assertSame(existing, result);
        verifyNoInteractions(accountServiceClient);
        verifyNoInteractions(eventPublisher);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processTransaction_TransferType_ThrowsException() {
        when(transactionRepository.findByIdempotencyKey("transfer"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(
                        "transfer",
                        new TransactionRequest(1L, new BigDecimal("10.00"),
                                Transaction.TransactionType.TRANSFER)
                )
        );

        assertEquals("Use the transfer endpoint for transfers", exception.getMessage());
        verifyNoInteractions(accountServiceClient);
    }

    @Test
    void processTransaction_InvalidAmount_ThrowsException() {
        when(transactionRepository.findByIdempotencyKey("bad"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.processTransaction(
                        "bad",
                        new TransactionRequest(1L, BigDecimal.ZERO,
                                Transaction.TransactionType.DEBIT)
                )
        );
        verifyNoInteractions(accountServiceClient);
    }

    @Test
    void transferMoney_Success() {
        when(transactionRepository.findByIdempotencyKey("transfer-key"))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.transferMoney(
                "transfer-key",
                new TransferRequest(1L, 2L, new BigDecimal("100.00"))
        );

        assertEquals(Transaction.TransactionType.TRANSFER, result.getType());
        assertEquals(1L, result.getAccountId());
        assertEquals(2L, result.getRelatedAccountId());
        verify(accountServiceClient).debit(1L, new BigDecimal("100.00"), "transfer-key-DEBIT");
        verify(accountServiceClient).credit(2L, new BigDecimal("100.00"), "transfer-key-CREDIT");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transferMoney_CreditFailure_CompensatesSourceAccount() {
        when(transactionRepository.findByIdempotencyKey("transfer-key"))
                .thenReturn(Optional.empty());
        doThrow(new RuntimeException("destination unavailable"))
                .when(accountServiceClient).credit(2L, new BigDecimal("100.00"),
                        "transfer-key-CREDIT");

        assertThrows(RuntimeException.class, () ->
                transactionService.transferMoney(
                        "transfer-key",
                        new TransferRequest(1L, 2L, new BigDecimal("100.00"))
                )
        );

        verify(accountServiceClient).credit(
                1L, new BigDecimal("100.00"), "transfer-key-COMPENSATION"
        );
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void transferMoney_SameAccount_ThrowsException() {
        when(transactionRepository.findByIdempotencyKey("same"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                transactionService.transferMoney(
                        "same",
                        new TransferRequest(1L, 1L, new BigDecimal("10.00"))
                )
        );
        verifyNoInteractions(accountServiceClient);
    }

    @Test
    void getTransactionById_NotFound() {
        when(transactionRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(404L));
    }

    @Test
    void refundDebit_CreatesCreditRefund() {
        Transaction original = new Transaction(
                "original", Transaction.TransactionType.DEBIT,
                new BigDecimal("30.00"), 1L, null, null
        );
        when(transactionRepository.findByIdempotencyKey("refund-key"))
                .thenReturn(Optional.empty());
        when(transactionRepository.findById(7L)).thenReturn(Optional.of(original));
        when(transactionRepository.existsByOriginalTransactionId(7L)).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction refund = transactionService.refundTransaction("refund-key", 7L);

        assertEquals(Transaction.TransactionType.CREDIT, refund.getType());
        assertEquals(7L, refund.getOriginalTransactionId());
        verify(accountServiceClient).credit(1L, new BigDecimal("30.00"), "refund-key");
    }

    @Test
    void generateReceipt_ReturnsAccountBalance() {
        Transaction transaction = new Transaction(
                "receipt-key",
                Transaction.TransactionType.DEBIT,
                new BigDecimal("20.00"),
                1L, null, null
        );

        ReflectionTestUtils.setField(transaction, "id", 3L);

        when(transactionRepository.findById(3L))
                .thenReturn(Optional.of(transaction));

        when(accountServiceClient.getAccount(1L)).thenReturn(
                new AccountServiceClient.AccountResponse(
                        1L, new BigDecimal("80.00")
                )
        );

        TransactionReceiptDto receipt =
                transactionService.generateReceipt(3L);

        assertEquals(3L, receipt.transactionId());
        assertEquals(new BigDecimal("80.00"),
                receipt.currentAccountBalance());
    }
}