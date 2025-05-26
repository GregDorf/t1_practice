package com.testprojgroup.t1_practice.service;

import com.testprojgroup.t1_practice.model.Account;

import java.util.List;

public interface AccountService {
    List<Account> getAllAccounts();
    Account getAccount(Long id);
    void saveAccount(Account account);
    void deleteAccount(Long id);
}