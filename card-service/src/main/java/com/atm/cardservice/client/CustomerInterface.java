package com.atm.cardservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerInterface {

    @GetMapping("/customer/{id}")
    void verifyCustomer(@PathVariable("id") Long customerId);
}