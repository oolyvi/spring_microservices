package com.atm.atmservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "card-service")
public interface CardInterface {

    @PostMapping("/card/authenticate")
    ResponseEntity<Void> authenticateCard(
            @RequestParam("cardNumber") String cardNumber,
            @RequestParam("pin") String pin
    );

    @GetMapping("/card/balance")
    ResponseEntity<BigDecimal> getBalanceByCardNumber(
            @RequestParam("cardNumber") String cardNumber
    );
}