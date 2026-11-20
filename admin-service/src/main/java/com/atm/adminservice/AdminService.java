package com.atm.adminservice;

import com.atm.adminservice.client.AccountInterface;
import com.atm.adminservice.client.AtmInterface;
import com.atm.adminservice.client.CardInterface;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final AccountInterface accountInterface;
    private final AtmInterface atmInterface;
    private final CardInterface cardInterface;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminService(
            AdminRepository adminRepository,
            AccountInterface accountInterface,
            AtmInterface atmInterface,
            CardInterface cardInterface
    ) {
        this.adminRepository = adminRepository;
        this.accountInterface = accountInterface;
        this.atmInterface = atmInterface;
        this.cardInterface = cardInterface;
    }

    public boolean authenticateAdmin(
            String username,
            String rawPassword
    ) {
        System.out.println("[AUTH] Username: " + username);

        var adminOptional = adminRepository.findByUsername(username);

        if (adminOptional.isEmpty()) {
            System.out.println("[AUTH] Admin NOT FOUND in database");
            return false;
        }

        Admin admin = adminOptional.get();

        System.out.println("[AUTH] Admin FOUND in database");

        boolean matches = passwordEncoder.matches(
                rawPassword,
                admin.getPasswordHash()
        );

        System.out.println("[AUTH] Password matches: " + matches);

        return matches;
    }

    @Transactional
    public void updateAdminProfile(
            Long adminId,
            String newUsername,
            String newEmail
    ) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Admin not found"));

        admin.setUsername(newUsername);
        admin.setEmail(newEmail);

        adminRepository.save(admin);
    }

    public void updateAccountLimit(
            Long accountId,
            BigDecimal newDailyLimit
    ) {
        if (newDailyLimit == null || newDailyLimit.signum() < 0) {
            throw new IllegalArgumentException("Invalid daily limit");
        }

        accountInterface.updateAccountLimit(
                accountId,
                newDailyLimit
        );
    }

    public void changeTerminalStatus(
            Long terminalId,
            String status
    ) {
        atmInterface.changeTerminalStatus(
                terminalId,
                status
        );
    }

    public void manualUnblockCard(Long cardId) {
        cardInterface.unblockCard(cardId);
    }

    public void manualBlockCard(Long cardId) {
        cardInterface.blockCard(cardId);
    }

    public void forceResetCardPin(
            Long cardId,
            String temporaryPinHash
    ) {
        cardInterface.forceResetCardPin(
                cardId,
                temporaryPinHash
        );
    }
}