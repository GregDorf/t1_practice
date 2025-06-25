package com.testprojgroup.t1_practice.scheduler;

import com.testprojgroup.t1_practice.model.*;
import com.testprojgroup.t1_practice.repository.AccountRepository;
import com.testprojgroup.t1_practice.repository.ClientRepository;
import com.testprojgroup.t1_practice.service.UnblockingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class UnblockTasksScheduler {
    private static final Logger log = LoggerFactory.getLogger(UnblockTasksScheduler.class);

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final UnblockingService unblockingService;

    @Value("${scheduler.unblock-clients.enabled:false}")
    private boolean clientUnblockTaskEnabled;

    @Value("${scheduler.unblock-clients.batch-size:10}")
    private int clientBatchSize;

    @Value("${scheduler.release-account-arrest.enabled:false}")
    private boolean accountArrestReleaseTaskEnabled;

    @Value("${scheduler.release-account-arrest.batch-size:5}")
    private int accountBatchSize;

    @Autowired
    public UnblockTasksScheduler(ClientRepository clientRepository,
                                 AccountRepository accountRepository,
                                 UnblockingService unblockingService) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.unblockingService = unblockingService;
    }

    @Scheduled(cron = "${scheduler.unblock-clients.cron}")
    @Transactional
    public void processClientUnblocks() {
        if (!clientUnblockTaskEnabled) {
            log.trace("Client unblock task is disabled.");
            return;
        }
        log.info("Starting scheduled task: Process Client Unblocks. Batch size: {}", clientBatchSize);

        List<Client> blockedClients = clientRepository.findByStatus(
                ClientStatusEnum.BLOCKED,
                PageRequest.of(0, clientBatchSize)
        );

        if (blockedClients.isEmpty()) {
            log.info("No blocked clients found to process.");
            return;
        }

        log.info("Found {} blocked clients to request unblocking.", blockedClients.size());
        for (Client client : blockedClients) {
            if (client.getClientId() == null) {
                log.warn("Client with internal ID {} has null clientId, skipping.", client.getId());
                continue;
            }
            UnblockClientResponse decision = unblockingService.requestClientUnblock(client.getClientId());
            if (decision != null && decision.isAllowUnblocking()) {
                log.info("Unblock allowed for client ID: {}. Updating status.", client.getClientId());
                client.setStatus(ClientStatusEnum.UNBLOCKED);
                clientRepository.save(client);
            } else {
                log.info("Unblock denied or error for client ID: {}", client.getClientId());
            }
        }
        log.info("Finished scheduled task: Process Client Unblocks.");
    }

    @Scheduled(cron = "${scheduler.release-account-arrest.cron}")
    @Transactional
    public void processAccountArrestReleases() {
        if (!accountArrestReleaseTaskEnabled) {
            log.trace("Account arrest release task is disabled.");
            return;
        }
        log.info("Starting scheduled task: Process Account Arrest Releases. Batch size: {}", accountBatchSize);

        List<Account> arrestedAccounts = accountRepository.findByStatus(
                AccountStatusEnum.ARRESTED,
                PageRequest.of(0, accountBatchSize)
        );

        if (arrestedAccounts.isEmpty()) {
            log.info("No arrested accounts found to process.");
            return;
        }

        log.info("Found {} arrested accounts to request arrest release.", arrestedAccounts.size());
        for (Account account : arrestedAccounts) {
            if (account.getAccountId() == null) {
                log.warn("Account with internal ID {} has null accountId, skipping.", account.getId());
                continue;
            }
            UnblockAccountResponse decision = unblockingService.requestAccountArrestRelease(account.getAccountId());
            if (decision != null && decision.isAllowUnblocking()) {
                log.info("Arrest release allowed for account ID: {}. Updating status to OPEN.", account.getAccountId());
                account.setStatus(AccountStatusEnum.OPEN);
                accountRepository.save(account);
            } else {
                log.info("Arrest release denied or error for account ID: {}", account.getAccountId());
            }
        }
        log.info("Finished scheduled task: Process Account Arrest Releases.");
    }
}