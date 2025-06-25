package com.testprojgroup.t1_practice.service.impl_transaction_processing_service;

import com.testprojgroup.t1_practice.config.TransactionConfig;
import com.testprojgroup.t1_practice.kafka.messages.TransactionAcceptMessage;
import com.testprojgroup.t1_practice.kafka.messages.TransactionRequestMessage;
import com.testprojgroup.t1_practice.kafka.transaction_request.TransactionAcceptProducer;
import com.testprojgroup.t1_practice.model.*;
import com.testprojgroup.t1_practice.service.AccountService;
import com.testprojgroup.t1_practice.service.ClientStatusService;
import com.testprojgroup.t1_practice.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionProcessingServiceImplTest {

    @Mock
    private AccountService accountService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TransactionAcceptProducer transactionProducer;
    @Mock
    private ClientStatusService clientStatusService;
    @Mock
    private TransactionConfig transactionConfig;
    @InjectMocks
    private TransactionProcessingServiceImpl transactionProcessingService;

    private TransactionRequestMessage requestMessage;
    private Account account;
    private Client client;
    private UUID accountUuid;
    private UUID clientUuid;
    private BigDecimal amount;
    private final Long ACCOUNT_DB_ID = 1L;

    @BeforeEach
    void setUp() {
        accountUuid = UUID.randomUUID();
        clientUuid = UUID.randomUUID();
        amount = new BigDecimal("100.00");
        requestMessage = new TransactionRequestMessage(clientUuid, accountUuid, amount);

        client = Client.builder()
                .clientId(clientUuid)
                .name("Proc")
                .surname("User")
                .status(ClientStatusEnum.UNBLOCKED)
                .build();

        account = Account.builder()
                .accountId(accountUuid)
                .client(client)
                .account(AccountTypeEnum.DEBIT)
                .status(AccountStatusEnum.OPEN)
                .balance(1000.0)
                .frozenAmount(0L)
                .build();
        ReflectionTestUtils.setField(account, "id", ACCOUNT_DB_ID);
    }

    @Test
    @DisplayName("Process transaction - successful case")
    void processTransaction_successful() {
        when(accountService.findByAccountId(accountUuid)).thenReturn(account);
        when(transactionConfig.getRejectThreshold()).thenReturn(3);
        when(transactionService.countRejectedTransactionsByAccountId(ACCOUNT_DB_ID)).thenReturn(0);

        Transaction createdTx = Transaction.builder()
                .transactionId(UUID.randomUUID())
                .account(account)
                .amount(amount)
                .status(TransactionStatusEnum.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();
        when(transactionService.createTransaction(account, amount)).thenReturn(createdTx);

        transactionProcessingService.processTransaction(requestMessage);

        verify(transactionService).createTransaction(account, amount);
        verify(accountService).adjustBalance(account, amount);

        ArgumentCaptor<TransactionAcceptMessage> captor = ArgumentCaptor.forClass(TransactionAcceptMessage.class);
        verify(transactionProducer).sendAcceptMessage(captor.capture());

        TransactionAcceptMessage sentMessage = captor.getValue();
        assertEquals(clientUuid, sentMessage.getClientId());
        assertEquals(accountUuid, sentMessage.getAccountId());
        assertEquals(createdTx.getTransactionId(), sentMessage.getTransactionId());
        assertNotNull(sentMessage.getTimestamp());
        assertEquals(0, amount.compareTo(sentMessage.getAmount()));
        assertEquals(account.getBalance(), sentMessage.getAccountBalance(), 0.001);
    }

    @Test
    @DisplayName("Process transaction - account status not OPEN, should return")
    void processTransaction_accountNotOpen() {
        account.setStatus(AccountStatusEnum.BLOCKED);
        when(accountService.findByAccountId(accountUuid)).thenReturn(account);

        transactionProcessingService.processTransaction(requestMessage);

        verify(accountService).findByAccountId(accountUuid);
        verify(transactionService, never()).createTransaction(any(), any());
        verify(accountService, never()).adjustBalance(any(), any());
        verify(transactionProducer, never()).sendAcceptMessage(any());
        verify(clientStatusService, never()).fetchStatusFromService2(any(), any());
    }

    @Test
    @DisplayName("Process transaction - client blacklisted (account or status null), should block and reject")
    void processTransaction_clientBlacklisted() {
        Account accountWithNullStatus = Account.builder().accountId(accountUuid).status(null).build();
        ReflectionTestUtils.setField(accountWithNullStatus, "id", ACCOUNT_DB_ID);
        when(accountService.findByAccountId(accountUuid)).thenReturn(accountWithNullStatus);

        ClientStatusResponse blacklistResponse = new ClientStatusResponse(
                clientUuid.toString(),
                accountUuid.toString(),
                "BLACKLISTED"
        );
        when(clientStatusService.fetchStatusFromService2(clientUuid, accountUuid)).thenReturn(blacklistResponse);

        transactionProcessingService.processTransaction(requestMessage);

        verify(clientStatusService).fetchStatusFromService2(clientUuid, accountUuid);
        verify(accountService).blockAccountAndClient(accountUuid, clientUuid);
        verify(transactionService).createRejectedTransaction(accountUuid, amount);
        verifyNoInteractions(transactionProducer);
    }

    @Test
    @DisplayName("Process transaction - reject threshold reached, should arrest and reject")
    void processTransaction_rejectThresholdReached() {
        when(accountService.findByAccountId(accountUuid)).thenReturn(account);
        when(transactionConfig.getRejectThreshold()).thenReturn(3);
        when(transactionService.countRejectedTransactionsByAccountId(ACCOUNT_DB_ID)).thenReturn(3);

        transactionProcessingService.processTransaction(requestMessage);

        verify(transactionService).createRejectedTransaction(accountUuid, amount);
        verify(accountService).updateAccountStatus(accountUuid, AccountStatusEnum.ARRESTED);
        verifyNoInteractions(transactionProducer);
        verify(accountService, never()).adjustBalance(any(Account.class), any(BigDecimal.class));
    }
}