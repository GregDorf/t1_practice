package com.testprojgroup.t1_practice.service.impl_transaction_result_processing_service;

import com.testprojgroup.t1_practice.kafka.messages.TransactionResultMessage;
import com.testprojgroup.t1_practice.model.*;
import com.testprojgroup.t1_practice.repository.AccountRepository;
import com.testprojgroup.t1_practice.repository.TransactionRepository;
import com.testprojgroup.t1_practice.service.TransactionResultProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.testprojgroup.t1_practice.model.TransactionStatusEnum.*;

@Service
@RequiredArgsConstructor
public class TransactionResultProcessingServiceImpl implements TransactionResultProcessingService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public void processResult(TransactionResultMessage message) {
        Optional<Transaction> transactionOpt = transactionRepository.findByTransactionId(message.getTransactionId());
        if (transactionOpt.isEmpty()) {
            return;
        }

        Transaction transaction = transactionOpt.get();
        Account account = transaction.getAccount();

        TransactionStatusEnum status;
        try {
            status = TransactionStatusEnum.valueOf(message.getStatus());
        } catch (IllegalArgumentException e) {
            return;
        }

        switch (status) {
            case ACCEPTED -> {
                transaction.setStatus(ACCEPTED);
                transactionRepository.save(transaction);
            }

            case REJECTED -> {
                transaction.setStatus(REJECTED);
                transactionRepository.save(transaction);

                BigDecimal newBalance = BigDecimal.valueOf(account.getBalance()).add(transaction.getAmount());
                account.setBalance(newBalance.doubleValue());
                accountRepository.save(account);
            }

            case BLOCKED -> {
                account.setStatus(AccountStatusEnum.BLOCKED);

                List<UUID> blockedTransactionIds = message.getBlockedTransactionIds();
                List<Transaction> blockedTransactions = transactionRepository.findAllByTransactionIdIn(blockedTransactionIds);
                BigDecimal totalFrozen = BigDecimal.ZERO;

                for (Transaction tx : blockedTransactions) {
                    tx.setStatus(BLOCKED);
                    transactionRepository.save(tx);
                    totalFrozen = totalFrozen.add(tx.getAmount());
                }

                account.setFrozenAmount(totalFrozen.longValue());
                BigDecimal updatedBalance = BigDecimal.valueOf(account.getBalance()).subtract(totalFrozen);
                account.setBalance(updatedBalance.doubleValue());

                accountRepository.save(account);
            }
        }
    }
}
