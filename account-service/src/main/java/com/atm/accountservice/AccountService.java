package com.atm.accountservice;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountOperationRepository operationRepository;

    public AccountService(AccountRepository accountRepository,
                          AccountOperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(AccountDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AccountDto> getAccountById(Long id) {
        return accountRepository.findById(id)
                .map(AccountDto::fromEntity);
    }

    @Transactional
    public AccountDto addAccount(Account account) {
        Account savedAccount = accountRepository.save(account);
        return AccountDto.fromEntity(savedAccount);
    }

    @Transactional
    public AccountDto replaceAccount(Long id, Account newAccount) {
        Account savedAccount = accountRepository.findById(id)
                .map(account -> {
                    account.setAccountNumber(newAccount.getAccountNumber());
                    account.setBalance(newAccount.getBalance());
                    account.setAccountType(newAccount.getAccountType());
                    account.setCustomerId(newAccount.getCustomerId());
                    account.setDailyLimit(newAccount.getDailyLimit());
                    account.setStatus(newAccount.getStatus());
                    return accountRepository.save(account);
                })
                .orElseGet(() -> {
                    newAccount.setId(null);
                    return accountRepository.save(newAccount);
                });

        return AccountDto.fromEntity(savedAccount);
    }

    @Transactional
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    @Transactional
    public void manualUnblockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    @Transactional
    public void manualBlockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setStatus(Account.AccountStatus.BLOCKED);
        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found with ID: " + accountId));

        return account.getBalance();
    }

    @Transactional
    public void debit(Long accountId, BigDecimal amount, String idempotencyKey) {
        applyOperation(accountId, amount, idempotencyKey, AccountOperation.OperationType.DEBIT);
    }

    @Transactional
    public void credit(Long accountId, BigDecimal amount, String idempotencyKey) {
        applyOperation(accountId, amount, idempotencyKey, AccountOperation.OperationType.CREDIT);
    }

    @Transactional
    public void updateDailyLimit(Long accountId, BigDecimal newLimit) {
        if (newLimit == null || newLimit.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Daily limit must not be negative");
        }
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found with ID: " + accountId));
        account.setDailyLimit(newLimit);
        accountRepository.save(account);
    }

    private void applyOperation(Long accountId,
                                BigDecimal amount,
                                String idempotencyKey,
                                AccountOperation.OperationType type) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found with ID: " + accountId));

        if (operationRepository.existsByIdempotencyKey(idempotencyKey)) {
            return;
        }

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }

        if (type == AccountOperation.OperationType.DEBIT) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient funds");
            }
            account.setBalance(account.getBalance().subtract(amount));
        } else {
            account.setBalance(account.getBalance().add(amount));
        }

        accountRepository.save(account);
        operationRepository.save(new AccountOperation(idempotencyKey, accountId, type, amount));
    }
}