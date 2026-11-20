package com.atm.atmservice;

import com.atm.atmservice.client.CardInterface;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/atm")
public class AtmController {

    private final CardInterface cardInterface;
    private final AtmTerminalService atmTerminalService;

    public AtmController(CardInterface cardInterface, AtmTerminalService atmTerminalService) {
        this.cardInterface = cardInterface;
        this.atmTerminalService = atmTerminalService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AtmLoginRequest request) {
        /*
        http://localhost:8083/atm/login

        {
            "cardNumber": "4532678912348812",
            "pin": "1234"
        }
         */
        atmTerminalService.authenticateAtmUser(request.cardNumber(), request.pin());
        return ResponseEntity.ok("[ATM_DEBUG]: Login successful! PIN verified and card is active.");
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getAtmBalance(@RequestParam String cardNumber) {
        /*
         http://localhost:8083/atm/balance?cardNumber=4532678912348812
         */
        BigDecimal balance;
        try {
            balance = cardInterface.getBalanceByCardNumber(cardNumber).getBody();
        } catch (FeignException.Forbidden e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Card is not active");
        } catch (FeignException.NotFound | FeignException.UnprocessableEntity e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Card or account not found");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Card service unavailable");
        }
        System.out.println("[ATM_DEBUG]: atm balance is " + balance + " for card number: " + cardNumber);
        return ResponseEntity.ok(balance);
    }
}