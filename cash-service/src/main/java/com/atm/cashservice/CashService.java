package com.atm.cashservice;

import com.atm.cashservice.event.CashEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CashService {

    private final CashRepository cashRepository;
    private final CashEventPublisher eventPublisher;

    public CashService(CashRepository cashRepository, CashEventPublisher eventPublisher) {
        this.cashRepository = cashRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Map<Integer, Integer> withdrawCash(Long atmId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        }

        List<Cash> cassettes = cashRepository.findByAtmIdOrderByDenominationDesc(atmId);
        if (cassettes.isEmpty()) {
            throw new IllegalStateException("No cash inventory found for ATM terminal ID: " + atmId);
        }

        int remaining = amount;
        Map<Integer, Integer> dispensedBills = new HashMap<>();
        Map<Integer, Cash> cashEntityMap = new HashMap<>();
        Map<Integer, Integer> cassetteCounts = new HashMap<>();

        for (Cash cassette : cassettes) {
            cashEntityMap.put(cassette.getDenomination(), cassette);
            cassetteCounts.put(cassette.getDenomination(), cassette.getQuantity());
        }

        for (Cash cassette : cassettes) {
            int denom = cassette.getDenomination();
            int availableCount = cassetteCounts.get(denom);

            if (remaining >= denom && availableCount > 0) {
                int take = Math.min(remaining / denom, availableCount);
                if (take > 0) {
                    dispensedBills.put(denom, take);
                    remaining -= take * denom;
                    cassetteCounts.put(denom, availableCount - take);
                }
            }
        }

        if (remaining > 0) {
            throw new IllegalStateException("Unable to dispense exact amount with available cash denominations. Remaining: " + remaining);
        }

        for (Map.Entry<Integer, Integer> entry : cassetteCounts.entrySet()) {
            int denom = entry.getKey();
            int newQty = entry.getValue();

            Cash cassette = cashEntityMap.get(denom);
            cassette.setQuantity(newQty);
            cashRepository.save(cassette);

            if (dispensedBills.containsKey(denom) && newQty <= cassette.getLowThreshold()) {
                sendLowMoneyNotification(atmId, denom, newQty, cassette.getLowThreshold());
            }
        }

        return dispensedBills;
    }

    private void sendLowMoneyNotification(Long atmId, int denomination, int quantity, int threshold) {
        System.out.println("ALERT: ATM ID " + atmId + " denomination " + denomination + " is low. Remaining: " + quantity);
        eventPublisher.publishCashLow(atmId, denomination, quantity, threshold);
    }
}