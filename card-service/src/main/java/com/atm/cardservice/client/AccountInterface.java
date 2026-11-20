package com.atm.cardservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "account-service")
public interface AccountInterface {

    @GetMapping("/account/{id}")
    void verifyAccount(@PathVariable("id") Long accountId);

    @GetMapping("/account/{id}/balance")
    BigDecimal getAccountBalance(@PathVariable("id") Long accountId);
}