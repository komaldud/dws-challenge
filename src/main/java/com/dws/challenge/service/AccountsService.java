package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    private final NotificationService notificationService;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    @Transactional
    public void transfer(String accountFromId, String accountToId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Retrieve accounts
        Account accountFrom = accountsRepository.getAccount(accountFromId);
        Account accountTo = accountsRepository.getAccount(accountToId);

        // Ensure thread-safety by locking accounts
        synchronized (accountFrom) {
            synchronized (accountTo) {
                // Ensure accountFrom has sufficient balance
                if (!accountFrom.withdraw(amount)) {
                    throw new RuntimeException("Insufficient balance in account " + accountFromId);
                }

                // Perform transfer
                accountTo.deposit(amount);

                // Send notifications to both account holders
                notificationService.notifyAboutTransfer(accountFrom,
                        "Transferred " + amount + " to account " + accountToId);
                notificationService.notifyAboutTransfer(accountTo,
                        "Received " + amount + " from account " + accountFromId);
            }
        }
    }
}
