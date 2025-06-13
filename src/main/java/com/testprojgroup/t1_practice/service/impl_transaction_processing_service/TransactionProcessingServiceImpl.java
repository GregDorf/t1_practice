package com.testprojgroup.t1_practice.service.impl_transaction_processing_service;


import com.testprojgroup.t1_practice.kafka.TransactionAcceptProducer;
import com.testprojgroup.t1_practice.kafka.messages.TransactionAcceptMessage;
import com.testprojgroup.t1_practice.kafka.messages.TransactionRequestMessage;
import com.testprojgroup.t1_practice.model.Account;
import com.testprojgroup.t1_practice.model.AccountStatusEnum;
import com.testprojgroup.t1_practice.model.Transaction;
import com.testprojgroup.t1_practice.service.AccountService;
import com.testprojgroup.t1_practice.service.TransactionProcessingService;
import com.testprojgroup.t1_practice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionProcessingServiceImpl implements TransactionProcessingService {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransactionAcceptProducer producer;

    public void processTransaction(TransactionRequestMessage msg) {
        Account account = accountService.findByAccountId(msg.getAccountId());

        if (account.getStatus() != AccountStatusEnum.OPEN) {
            return;
        }

        Transaction tx = transactionService.createTransaction(account, msg.getAmount());
        accountService.adjustBalance(account, msg.getAmount());

        TransactionAcceptMessage acceptMessage = new TransactionAcceptMessage(
                account.getClient().getClientId(),
                account.getAccountId(),
                tx.getTransactionId(),
                Instant.now().toEpochMilli(),
                msg.getAmount(),
                account.getBalance()
        );

        producer.sendAcceptMessage(acceptMessage);
    }
}