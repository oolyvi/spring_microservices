package com.atm.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "account-service")
public interface AccountInterface {

    @PutMapping("/account/{id}/limit")
    ResponseEntity<String> updateAccountLimit(
            @PathVariable("id") Long accountId,
            @RequestParam("limit") BigDecimal limit
    );
}