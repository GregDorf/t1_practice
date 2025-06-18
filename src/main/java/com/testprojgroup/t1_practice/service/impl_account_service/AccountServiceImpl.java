package com.testprojgroup.t1_practice.service.impl_account_service;


import com.testprojgroup.logging.annotations.Cached;
import com.testprojgroup.logging.annotations.LogDataSourceError;
import com.testprojgroup.logging.annotations.MetricTrack;
import com.testprojgroup.t1_practice.model.Account;
import com.testprojgroup.t1_practice.model.AccountStatusEnum;
import com.testprojgroup.t1_practice.model.Client;
import com.testprojgroup.t1_practice.model.ClientStatusEnum;
import com.testprojgroup.t1_practice.repository.AccountRepository;
import com.testprojgroup.t1_practice.repository.ClientRepository;
import com.testprojgroup.t1_practice.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Primary
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    @MetricTrack
    @Cached(cacheName="Accounts_List")
    @LogDataSourceError
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @MetricTrack
    @Cached(cacheName="Account")
    @LogDataSourceError
    public Account getAccount(Long id) {
        return accountRepository.getAccountById(id);
    }

    @MetricTrack
    @LogDataSourceError
    public void saveAccount(Account account) {
        accountRepository.save(account);
    }

    @MetricTrack
    @LogDataSourceError
    @Transactional
    public void deleteAccount(Long id) {
        accountRepository.deleteAccountById(id);
    }

    public Account findByAccountId(UUID accountId) {
        return accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public void adjustBalance(Account account, BigDecimal delta) {
        account.setBalance(account.getBalance() + delta.doubleValue());
        accountRepository.save(account);
    }

    public void blockAccountAndClient(UUID accountId, UUID clientId) {
        Account account = accountRepository.findByAccountId(accountId).orElseThrow();
        Client client = clientRepository.findByClientId(clientId).orElseThrow();

        account.setStatus(AccountStatusEnum.BLOCKED);
        client.setStatus(ClientStatusEnum.BLOCKED);

        accountRepository.save(account);
        clientRepository.save(client);
    }

    public void updateAccountStatus(UUID accountId, AccountStatusEnum newStatus) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setStatus(newStatus);
        accountRepository.save(account);
    }
}
