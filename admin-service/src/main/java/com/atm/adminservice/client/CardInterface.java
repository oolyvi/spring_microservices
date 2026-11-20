package com.atm.adminservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "card-service")
public interface CardInterface {

    @PostMapping("/card/{cardId}/unblock")
    ResponseEntity<String> unblockCard(@PathVariable("cardId") Long cardId);

    @PostMapping("/card/{cardId}/block")
    ResponseEntity<String> blockCard(@PathVariable("cardId") Long cardId);

    @PostMapping("/card/{cardId}/reset-pin")
    ResponseEntity<String> forceResetCardPin(
            @PathVariable("cardId") Long cardId,
            @RequestParam("temporaryPinHash") String temporaryPinHash
    );
}