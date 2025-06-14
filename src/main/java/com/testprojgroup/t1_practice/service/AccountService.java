package com.testprojgroup.t1_practice.service;

import com.testprojgroup.t1_practice.model.Account;
import com.testprojgroup.t1_practice.model.AccountStatusEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<Account> getAllAccounts();
    Account getAccount(Long id);
    void saveAccount(Account account);
    void deleteAccount(Long id);

    Account findByAccountId(UUID accountId);

    void adjustBalance(Account account, BigDecimal amount);

    public void blockAccountAndClient(UUID accountId, UUID clientId);

    void updateAccountStatus(UUID accountId, AccountStatusEnum newStatus);

}