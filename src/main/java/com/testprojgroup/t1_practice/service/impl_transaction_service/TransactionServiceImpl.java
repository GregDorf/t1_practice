package com.testprojgroup.t1_practice.service.impl_transaction_service;

import com.testprojgroup.logging.annotations.Cached;
import com.testprojgroup.logging.annotations.LogDataSourceError;
import com.testprojgroup.logging.annotations.MetricTrack;
import com.testprojgroup.t1_practice.model.Account;
import com.testprojgroup.t1_practice.model.Transaction;
import com.testprojgroup.t1_practice.model.TransactionStatusEnum;
import com.testprojgroup.t1_practice.repository.TransactionRepository;
import com.testprojgroup.t1_practice.service.AccountService;
import com.testprojgroup.t1_practice.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Primary
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

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

    public Transaction createTransaction(Account account, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(amount);
        tx.setStatus(TransactionStatusEnum.REQUESTED);
        tx.setTransactionId(UUID.randomUUID());
        return transactionRepository.save(tx);
    }

    public Transaction createRejectedTransaction(UUID accountId, BigDecimal amount) {
        Transaction tx = new Transaction();
        Account account = accountService.findByAccountId(accountId);
        tx.setTransactionId(UUID.randomUUID());
        tx.setAccount(accountService.findByAccountId(accountId));
        tx.setAmount(amount);
        tx.setStatus(TransactionStatusEnum.REJECTED);

        return transactionRepository.save(tx);
    }

    public Transaction createAcceptedTransaction(Account account, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setTransactionId(UUID.randomUUID());
        tx.setAccount(account);
        tx.setAmount(amount);
        tx.setStatus(TransactionStatusEnum.ACCEPTED);

        return transactionRepository.save(tx);
    }

    public int countRejectedTransactionsByAccountId(Long accountId) {
        return transactionRepository.countByAccountIdAndStatus(accountId, TransactionStatusEnum.REJECTED);
    }
}
