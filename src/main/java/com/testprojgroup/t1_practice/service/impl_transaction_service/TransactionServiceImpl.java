package com.testprojgroup.t1_practice.service.impl_transaction_service;

import com.testprojgroup.t1_practice.aop.annotation.Cached;
import com.testprojgroup.t1_practice.aop.annotation.LogDataSourceError;
import com.testprojgroup.t1_practice.aop.annotation.MetricTrack;
import com.testprojgroup.t1_practice.model.Transaction;
import com.testprojgroup.t1_practice.repository.TransactionRepository;
import com.testprojgroup.t1_practice.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    @MetricTrack
    @Cached(cacheName="Transactions_List")
    @LogDataSourceError
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @MetricTrack
    @Cached(cacheName="Transaction")
    @LogDataSourceError
    public Transaction getTransaction(Long id) {
        return transactionRepository.getTransactionById(id);
    }

    @MetricTrack
    @LogDataSourceError
    public void createTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @MetricTrack
    @LogDataSourceError
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
