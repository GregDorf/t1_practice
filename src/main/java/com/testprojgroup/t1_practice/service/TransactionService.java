package com.testprojgroup.t1_practice.service;

import com.testprojgroup.t1_practice.model.Account;
import com.testprojgroup.t1_practice.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction getTransaction(Long id);
    void createTransaction(Transaction transaction);
    void deleteTransaction(Long id);

    List<Transaction> getAllTransactions();

    Transaction createTransaction(Account account, BigDecimal amount);

    public Transaction createRejectedTransaction(UUID accountId, BigDecimal amount);

    int countRejectedTransactionsByAccountId(Long accountId);
}
