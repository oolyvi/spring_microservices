package com.atm.transactionservice;

import com.atm.transactionservice.client.AccountServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private AccountServiceClient accountServiceClient;

    @Test
    void executeTransaction_Success() throws Exception {
        String key = "test-guid-key";
        Transaction transaction = new Transaction(
                key, Transaction.TransactionType.DEBIT,
                new BigDecimal("150.00"), 1L, null, null
        );

        when(transactionService.processTransaction(eq(key), any(TransactionRequest.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/transaction/execute")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "amount": 150.00,
                                  "type": "DEBIT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idempotencyKey").value(key))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.accountId").value(1));
    }

    @Test
    void executeTransaction_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/transaction/execute")
                        .header("Idempotency-Key", "bad-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": 1,
                                  "amount": 0,
                                  "type": "DEBIT"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransactionReceipt_Success() throws Exception {
        TransactionReceiptDto receipt = new TransactionReceiptDto(
                1L, "key-xyz", 1L, "DEBIT",
                new BigDecimal("50.00"), LocalDateTime.now(),
                new BigDecimal("450.00")
        );
        when(transactionService.generateReceipt(1L)).thenReturn(receipt);

        mockMvc.perform(get("/transaction/{id}/receipt", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1))
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.currentAccountBalance").value(450.00));
    }

    @Test
    void getTransactionById_NotFound() throws Exception {
        when(transactionService.getTransactionById(99L))
                .thenThrow(new TransactionNotFoundException(99L));

        mockMvc.perform(get("/transaction/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void transferFunds_Success() throws Exception {
        Transaction transaction = new Transaction(
                "transfer-key", Transaction.TransactionType.TRANSFER,
                new BigDecimal("25.00"), 1L, 2L, null
        );
        when(transactionService.transferMoney(eq("transfer-key"), any(TransferRequest.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/transaction/transfer")
                        .header("Idempotency-Key", "transfer-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": 1,
                                  "destinationAccountId": 2,
                                  "amount": 25.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.relatedAccountId").value(2));
    }
}