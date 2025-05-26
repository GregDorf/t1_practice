package com.testprojgroup.t1_practice.service;

import com.testprojgroup.t1_practice.model.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction getTransaction(Long id);
    void createTransaction(Transaction transaction);
    void deleteTransaction(Long id);

    List<Transaction> getAllTransactions();
}
