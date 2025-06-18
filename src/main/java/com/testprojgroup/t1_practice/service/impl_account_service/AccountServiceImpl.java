package com.testprojgroup.t1_practice.service.impl_account_service;

import com.testprojgroup.t1_practice.aop.annotation.Cached;
import com.testprojgroup.t1_practice.aop.annotation.LogDataSourceError;
import com.testprojgroup.t1_practice.aop.annotation.MetricTrack;
import com.testprojgroup.t1_practice.model.Account;
import com.testprojgroup.t1_practice.repository.AccountRepository;
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
}
