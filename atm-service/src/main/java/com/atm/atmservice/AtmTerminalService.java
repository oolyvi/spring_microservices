package com.atm.atmservice;

import com.atm.atmservice.client.CardInterface;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AtmTerminalService {

    private final AtmTerminalRepository atmTerminalRepository;
    private final CardInterface cardInterface;

    public AtmTerminalService(AtmTerminalRepository atmTerminalRepository, CardInterface cardInterface) {
        this.atmTerminalRepository = atmTerminalRepository;
        this.cardInterface = cardInterface;
    }

    public AtmTerminal addTerminal(AtmTerminal terminal) {
        return atmTerminalRepository.save(terminal);
    }

    public List<AtmTerminal> getAllTerminals() {
        return atmTerminalRepository.findAll();
    }

    public Optional<AtmTerminal> getTerminalById(Long id) {
        return atmTerminalRepository.findById(id);
    }

    @Transactional
    public AtmTerminal changeTerminalStatus(Long terminalId, String status) {
        AtmTerminal.AtmTerminalStatus newStatus;
        try {
            newStatus = AtmTerminal.AtmTerminalStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status. Allowed: ACTIVE, MAINTENANCE, OFFLINE");
        }
        AtmTerminal terminal = atmTerminalRepository.findById(terminalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Terminal not found"));
        terminal.setStatus(newStatus);
        return atmTerminalRepository.save(terminal);
    }

    public void authenticateAtmUser(String cardNumber, String pin) {
        try {
            cardInterface.authenticateCard(cardNumber, pin);
        } catch (FeignException.Unauthorized | FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid card or PIN");
        } catch (FeignException.Forbidden e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Card is not active");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Card service unavailable");
        }
    }

    @Transactional
    public AtmTerminal dispense(Long terminalId, Map<Integer, Integer> bills) {
        AtmTerminal t = atmTerminalRepository.findById(terminalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Terminal not found"));

        for (Map.Entry<Integer, Integer> e : bills.entrySet()) {
            int qty = e.getValue();
            switch (e.getKey()) {
                case 1 -> t.setCassette1Count(subtract(t.getCassette1Count(), qty));
                case 5 -> t.setCassette5Count(subtract(t.getCassette5Count(), qty));
                case 10 -> t.setCassette10Count(subtract(t.getCassette10Count(), qty));
                case 20 -> t.setCassette20Count(subtract(t.getCassette20Count(), qty));
                case 50 -> t.setCassette50Count(subtract(t.getCassette50Count(), qty));
                case 100 -> t.setCassette100Count(subtract(t.getCassette100Count(), qty));
                case 200 -> t.setCassette200Count(subtract(t.getCassette200Count(), qty));
                case 500 -> t.setCassette500Count(subtract(t.getCassette500Count(), qty));
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unsupported denomination: " + e.getKey());
            }
        }
        t.setUpdatedAt(LocalDateTime.now());
        return atmTerminalRepository.save(t);
    }

    private int subtract(int current, int qty) {
        if (qty < 0 || qty > current) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough bills in cassette");
        }
        return current - qty;
    }
}