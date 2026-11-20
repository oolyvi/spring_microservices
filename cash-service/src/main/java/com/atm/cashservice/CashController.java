package com.atm.cashservice;

import com.atm.cashservice.client.AtmTerminalClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cash")
public class CashController {

    private final CashService cashService;
    private final AtmTerminalClient atmTerminalClient;

    public CashController(CashService cashService, AtmTerminalClient atmTerminalClient) {
        this.cashService = cashService;
        this.atmTerminalClient = atmTerminalClient;
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<Integer, Integer>> withdrawCash(
            @RequestParam Long atmId,
            @RequestParam int amount) {
         /*
         http://localhost:8085/cash/withdraw?atmId=1&amount=600
         */
        Map<Integer, Integer> breakdown = cashService.withdrawCash(atmId, amount);

        try {
            atmTerminalClient.dispense(atmId, breakdown);
        } catch (Exception e) {
            System.err.println("[ATM_DEBUG]: terminal sync failed for atm id " + atmId + ": " + e.getMessage());
        }

        System.out.println("[ATM_DEBUG]: " + amount + " is withdrawed from atm id: " + atmId);

        return ResponseEntity.ok(breakdown);
    }
}