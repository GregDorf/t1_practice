package com.testprojgroup.t1_practice.service.impl_account_service;

import com.testprojgroup.t1_practice.aop.annotation.LogDataSourceError;
import com.testprojgroup.t1_practice.model.Account;
import com.testprojgroup.t1_practice.repository.AccountRepository;
import com.testprojgroup.t1_practice.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Primary
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @LogDataSourceError
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @LogDataSourceError
    public Account getAccount(Long id) {
        return accountRepository.getAccountById(id);
    }

    @LogDataSourceError
    public void saveAccount(Account account) {
        accountRepository.save(account);
    }

    @LogDataSourceError
    @Transactional
    public void deleteAccount(Long id) {
        accountRepository.deleteAccountById(id);
    }
}
