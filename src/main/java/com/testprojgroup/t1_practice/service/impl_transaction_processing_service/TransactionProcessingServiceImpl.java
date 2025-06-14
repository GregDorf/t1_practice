package com.testprojgroup.t1_practice.service.impl_transaction_processing_service;


import com.testprojgroup.t1_practice.config.TransactionConfig;
import com.testprojgroup.t1_practice.kafka.messages.TransactionAcceptMessage;
import com.testprojgroup.t1_practice.kafka.messages.TransactionRequestMessage;
import com.testprojgroup.t1_practice.kafka.transaction_request.TransactionAcceptProducer;
import com.testprojgroup.t1_practice.model.Account;
import com.testprojgroup.t1_practice.model.AccountStatusEnum;
import com.testprojgroup.t1_practice.model.ClientStatusResponse;
import com.testprojgroup.t1_practice.model.Transaction;
import com.testprojgroup.t1_practice.service.AccountService;
import com.testprojgroup.t1_practice.service.ClientStatusService;
import com.testprojgroup.t1_practice.service.TransactionProcessingService;
import com.testprojgroup.t1_practice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionProcessingServiceImpl implements TransactionProcessingService {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransactionAcceptProducer transactionProducer;
    private final ClientStatusService clientStatusService;
    private final TransactionConfig transactionConfig;

    public void processTransaction(TransactionRequestMessage msg) {
        Account account = accountService.findByAccountId(msg.getAccountId());

        if (account == null || account.getStatus() == null) {
            ClientStatusResponse response = clientStatusService.fetchStatusFromService2(msg.getClientId(), msg.getAccountId());

            if ("BLACKLISTED".equals(response.getStatus())) {
                accountService.blockAccountAndClient(msg.getAccountId(), msg.getClientId());

                transactionService.createRejectedTransaction(msg.getAccountId(), msg.getAmount());

                return;
            }
        }

        if (Objects.requireNonNull(account).getStatus() != AccountStatusEnum.OPEN) {
            return;
        }

        int rejectedCount = transactionService.countRejectedTransactionsByAccountId(account.getId());

        if (rejectedCount >= transactionConfig.getRejectThreshold()) {
            transactionService.createRejectedTransaction(account.getAccountId(), msg.getAmount());
            accountService.updateAccountStatus(account.getAccountId(), AccountStatusEnum.ARRESTED);
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

        transactionProducer.sendAcceptMessage(acceptMessage);
    }
}
