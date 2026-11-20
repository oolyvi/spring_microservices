package com.atm.cardservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/card")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<CardDto>> getAllCards() {
        /*
         http://localhost:8084/card/all
         */
        System.out.println("[CARD_SERVICE_DEBUG]: get all cards");

        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        /*
         http://localhost:8084/card/1
         */
        System.out.println("[CARD_SERVICE_DEBUG]: get card for id: " + id);

        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @PostMapping("/add")
    public ResponseEntity<CardDto> addCard(@RequestBody CreateCardRequest request) {
        /*
         http://localhost:8084/card/add

         {
            "cardNumber": "4532678912341221",
            "pin": "6789",
            "cardType": "DEBIT",
            "customerId": "1",
            "accountId": "1"
         }
         */
        CardDto created = cardService.addCard(request.cardNumber(), request.pin(), request.cardType(), request.customerId(), request.accountId());

        System.out.println("[CARD_SERVICE_DEBUG]: add card for id: " + created.id());

        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/verify-pin")
    public ResponseEntity<Boolean> verifyPin(@PathVariable Long id, @RequestBody PinVerifyRequest request) {
        /*
        http://localhost:8084/card/1/verify-pin

         {
            "pin": "1234"
         }
         */
        boolean isValid = cardService.verifyPin(id, request.pin());

        System.out.println("[CARD_SERVICE_DEBUG]: card pin verified for id: " + id);

        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/{cardId}/pin-change")
    public ResponseEntity<String> changePin(@PathVariable Long cardId, @RequestBody PinChangeDto request) {
        /*
        http://localhost:8084/card/1/pin-change

         {
            "oldPin": "1234",
            "newPin": "4321"
         }
         */
        cardService.changePin(cardId, request.getOldPin(), request.getNewPin());

        System.out.println("[CARD_SERVICE_DEBUG]: card PIN changed for id: " + cardId);

        return ResponseEntity.ok("Card PIN changed successfully");
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<String> blockCard(@PathVariable Long cardId) {
        /*
        http://localhost:8084/card/1/block
         */
        cardService.blockCard(cardId);
        System.out.println("[CARD_SERVICE_DEBUG]: card blocked for id: " + cardId);
        return ResponseEntity.ok("Card has been blocked.");
    }

    @PostMapping("/{cardId}/unblock")
    public ResponseEntity<String> unblockCard(@PathVariable Long cardId) {
        /*
        http://localhost:8084/card/1/unblock
         */
        cardService.unblockCard(cardId);
        System.out.println("[CARD_SERVICE_DEBUG]: card unblocked for id: " + cardId);
        return ResponseEntity.ok("Card has been unblocked.");
    }

    @PostMapping("/{cardId}/reset-pin")
    public ResponseEntity<String> forceResetPin(
            @PathVariable Long cardId,
            @RequestParam("temporaryPinHash") String temporaryPinHash
    ) {
        /*
        http://localhost:8084/card/1/reset-pin?temporaryPinHash=<bcrypt hash of the temporary PIN>
         */
        cardService.forceResetPin(cardId, temporaryPinHash);
        System.out.println("[CARD_SERVICE_DEBUG]: card PIN reset for id: " + cardId);
        return ResponseEntity.ok("Card PIN reset completed.");
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Void> authenticateCard(
            @RequestParam String cardNumber,
            @RequestParam String pin
    ) {
        cardService.authenticateCard(cardNumber, pin);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalanceByCardNumber(
            @RequestParam String cardNumber
    ) {
        BigDecimal balance = cardService.getBalanceByCardNumber(cardNumber);

        return ResponseEntity.ok(balance);
    }
}