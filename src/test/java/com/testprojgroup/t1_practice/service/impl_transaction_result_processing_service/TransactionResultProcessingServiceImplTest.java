package com.testprojgroup.t1_practice.service.impl_transaction_result_processing_service;

import com.testprojgroup.t1_practice.kafka.messages.TransactionResultMessage;
import com.testprojgroup.t1_practice.model.*;
import com.testprojgroup.t1_practice.repository.AccountRepository;
import com.testprojgroup.t1_practice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionResultProcessingServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @InjectMocks
    private TransactionResultProcessingServiceImpl service;

    private Transaction transaction;
    private Account account;
    private UUID transactionUuid;
    private UUID accountUuid;

    @BeforeEach
    void setUp() {
        transactionUuid = UUID.randomUUID();
        accountUuid = UUID.randomUUID();

        Client client = Client.builder().clientId(UUID.randomUUID()).name("Res").surname("User").build();

        account = Account.builder()
                .accountId(accountUuid)
                .client(client)
                .account(AccountTypeEnum.DEBIT)
                .balance(1000.0)
                .status(AccountStatusEnum.OPEN)
                .frozenAmount(0L)
                .build();

        transaction = Transaction.builder()
                .transactionId(transactionUuid)
                .account(account)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatusEnum.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Process result - ACCEPTED")
    void processResult_accepted() {
        TransactionResultMessage message = new TransactionResultMessage(transactionUuid, accountUuid, "ACCEPTED", null);
        when(transactionRepository.findByTransactionId(transactionUuid)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        service.processResult(message);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertEquals(TransactionStatusEnum.ACCEPTED, captor.getValue().getStatus());
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Process result - REJECTED, should revert balance")
    void processResult_rejected() {
        TransactionResultMessage message = new TransactionResultMessage(transactionUuid, accountUuid, "REJECTED", null);
        when(transactionRepository.findByTransactionId(transactionUuid)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        double initialBalance = account.getBalance();
        BigDecimal txAmount = transaction.getAmount();

        service.processResult(message);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertEquals(TransactionStatusEnum.REJECTED, txCaptor.getValue().getStatus());

        ArgumentCaptor<Account> accCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accCaptor.capture());
        assertEquals(initialBalance + txAmount.doubleValue(), accCaptor.getValue().getBalance(), 0.001);
    }

    @Test
    @DisplayName("Process result - BLOCKED")
    void processResult_blocked() {
        UUID otherBlockedTxUuid = UUID.randomUUID();
        List<UUID> blockedIdsInMessage = List.of(transactionUuid, otherBlockedTxUuid);
        TransactionResultMessage message = new TransactionResultMessage(transactionUuid, accountUuid, "BLOCKED", blockedIdsInMessage);

        Transaction otherTx = Transaction.builder()
                .transactionId(otherBlockedTxUuid)
                .account(account)
                .amount(new BigDecimal("50.00"))
                .status(TransactionStatusEnum.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findByTransactionId(transactionUuid)).thenReturn(Optional.of(transaction));
        when(transactionRepository.findAllByTransactionIdIn(blockedIdsInMessage)).thenReturn(List.of(transaction, otherTx));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        double initialBalance = account.getBalance();
        BigDecimal totalFrozenAmount = transaction.getAmount().add(otherTx.getAmount());

        service.processResult(message);

        ArgumentCaptor<Account> accCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accCaptor.capture());
        Account savedAccount = accCaptor.getValue();
        assertEquals(AccountStatusEnum.BLOCKED, savedAccount.getStatus());
        assertEquals(totalFrozenAmount.longValue(), savedAccount.getFrozenAmount());
        assertEquals(initialBalance - totalFrozenAmount.doubleValue(), savedAccount.getBalance(), 0.001);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(txCaptor.capture());
        List<Transaction> savedTransactions = txCaptor.getAllValues();
        assertTrue(savedTransactions.stream().allMatch(t -> t.getStatus() == TransactionStatusEnum.BLOCKED));
        assertTrue(savedTransactions.stream().anyMatch(t -> t.getTransactionId().equals(transactionUuid)));
        assertTrue(savedTransactions.stream().anyMatch(t -> t.getTransactionId().equals(otherBlockedTxUuid)));
    }

    @Test
    @DisplayName("Process result - transaction not found, should return")
    void processResult_transactionNotFound() {
        TransactionResultMessage message = new TransactionResultMessage(transactionUuid, accountUuid, "ACCEPTED", null);
        when(transactionRepository.findByTransactionId(transactionUuid)).thenReturn(Optional.empty());

        service.processResult(message);

        verify(transactionRepository).findByTransactionId(transactionUuid);
        verifyNoMoreInteractions(transactionRepository, accountRepository);
    }

    @Test
    @DisplayName("Process result - invalid status in message, should return")
    void processResult_invalidStatus() {
        TransactionResultMessage message = new TransactionResultMessage(transactionUuid, accountUuid, "INVALID_STATUS_XYZ", null);
        when(transactionRepository.findByTransactionId(transactionUuid)).thenReturn(Optional.of(transaction));

        service.processResult(message);

        verify(transactionRepository).findByTransactionId(transactionUuid);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }
}