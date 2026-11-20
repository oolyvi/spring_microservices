package com.atm.cashservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "atm-service")
public interface AtmTerminalClient {

    @PutMapping("/atm/terminal/{id}/dispense")
    void dispense(@PathVariable("id") Long terminalId, @RequestBody Map<Integer, Integer> bills);
}