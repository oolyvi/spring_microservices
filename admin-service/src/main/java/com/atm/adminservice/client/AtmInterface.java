package com.atm.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "atm-service")
public interface AtmInterface {

    @PutMapping("/atm/terminal/{terminalId}/status")
    ResponseEntity<String> changeTerminalStatus(
            @PathVariable("terminalId") Long terminalId,
            @RequestParam("status") String status
    );
}