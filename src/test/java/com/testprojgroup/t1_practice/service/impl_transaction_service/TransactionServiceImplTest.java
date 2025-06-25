package com.testprojgroup.t1_practice.service.impl_transaction_service;

import com.testprojgroup.t1_practice.model.*;
import com.testprojgroup.t1_practice.repository.TransactionRepository;
import com.testprojgroup.t1_practice.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account testAccount;
    private UUID testAccountUuid;
    private final Long ACCOUNT_DB_ID = 1L;
    private final Long DEFAULT_TRANSACTION_DB_ID = 10L;

    @BeforeEach
    void setUp() {
        testAccountUuid = UUID.randomUUID();
        Client client = Client.builder().clientId(UUID.randomUUID()).name("TxServ").surname("Client").build();
        testAccount = Account.builder()
                .accountId(testAccountUuid)
                .client(client)
                .account(AccountTypeEnum.CREDIT)
                .status(AccountStatusEnum.OPEN)
                .balance(500.0)
                .build();
        ReflectionTestUtils.setField(testAccount, "id", ACCOUNT_DB_ID);
    }

    private void setupMockSaveForTransactionRepository() {
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txToSave = invocation.getArgument(0);
            if (txToSave.getId() == null) {
                ReflectionTestUtils.setField(txToSave, "id", DEFAULT_TRANSACTION_DB_ID);
            }
            return txToSave;
        });
    }

    @Test
    @DisplayName("getAllTransactions - success")
    void getAllTransactions_success() {
        Transaction sampleTx = Transaction.builder().transactionId(UUID.randomUUID()).createdAt(LocalDateTime.now()).build();
        List<Transaction> expectedList = Collections.singletonList(sampleTx);
        when(transactionRepository.findAll()).thenReturn(expectedList);

        List<Transaction> actualList = transactionService.getAllTransactions();
        assertEquals(expectedList, actualList);
        verify(transactionRepository).findAll();
    }

    @Test
    @DisplayName("getTransaction by DB ID - success")
    void getTransaction_byDbId_success() {
        Transaction sampleTx = Transaction.builder().transactionId(UUID.randomUUID()).createdAt(LocalDateTime.now()).build();
        ReflectionTestUtils.setField(sampleTx, "id", DEFAULT_TRANSACTION_DB_ID);
        when(transactionRepository.getTransactionById(DEFAULT_TRANSACTION_DB_ID)).thenReturn(sampleTx);

        Transaction found = transactionService.getTransaction(DEFAULT_TRANSACTION_DB_ID);
        assertEquals(sampleTx, found);
        verify(transactionRepository).getTransactionById(DEFAULT_TRANSACTION_DB_ID);
    }

    @Test
    @DisplayName("createTransaction (with Transaction object) - success")
    void createTransaction_withObject_success() {
        setupMockSaveForTransactionRepository();
        Transaction txToSave = Transaction.builder().transactionId(UUID.randomUUID()).createdAt(LocalDateTime.now()).build();

        transactionService.createTransaction(txToSave);

        verify(transactionRepository).save(txToSave);
        assertNotNull(txToSave.getId());
    }

    @Test
    @DisplayName("deleteTransaction by DB ID - success")
    void deleteTransaction_byDbId_success() {
        transactionService.deleteTransaction(DEFAULT_TRANSACTION_DB_ID);
        verify(transactionRepository).deleteById(DEFAULT_TRANSACTION_DB_ID);
    }


    @Test
    @DisplayName("createTransaction (Account, Amount) - creates REQUESTED transaction")
    void createTransaction_withAccountAndAmount_createsRequested() {
        setupMockSaveForTransactionRepository();
        BigDecimal amount = new BigDecimal("100.50");

        Transaction tx = transactionService.createTransaction(testAccount, amount);

        assertNotNull(tx.getTransactionId());
        assertEquals(testAccount, tx.getAccount());
        assertEquals(0, amount.compareTo(tx.getAmount()));
        assertEquals(TransactionStatusEnum.REQUESTED, tx.getStatus());
        assertNotNull(tx.getCreatedAt());
        assertNotNull(tx.getId());
        verify(transactionRepository).save(tx);
    }

    @Test
    @DisplayName("createRejectedTransaction - creates REJECTED transaction")
    void createRejectedTransaction_createsRejected() {
        setupMockSaveForTransactionRepository();
        BigDecimal amount = new BigDecimal("75.25");
        when(accountService.findByAccountId(testAccountUuid)).thenReturn(testAccount);

        Transaction tx = transactionService.createRejectedTransaction(testAccountUuid, amount);

        assertNotNull(tx.getTransactionId());
        assertEquals(testAccount, tx.getAccount());
        assertEquals(0, amount.compareTo(tx.getAmount()));
        assertEquals(TransactionStatusEnum.REJECTED, tx.getStatus());
        assertNotNull(tx.getCreatedAt());
        assertNotNull(tx.getId());
        verify(accountService, times(1)).findByAccountId(testAccountUuid);
        verify(transactionRepository).save(tx);
    }

    @Test
    @DisplayName("createAcceptedTransaction - creates ACCEPTED transaction")
    void createAcceptedTransaction_createsAccepted() {
        setupMockSaveForTransactionRepository();
        BigDecimal amount = new BigDecimal("120.00");

        Transaction tx = transactionService.createAcceptedTransaction(testAccount, amount);

        assertNotNull(tx.getTransactionId());
        assertEquals(testAccount, tx.getAccount());
        assertEquals(0, amount.compareTo(tx.getAmount()));
        assertEquals(TransactionStatusEnum.ACCEPTED, tx.getStatus());
        assertNotNull(tx.getCreatedAt());
        assertNotNull(tx.getId());
        verify(transactionRepository).save(tx);
    }

    @Test
    @DisplayName("countRejectedTransactionsByAccountId - calls repository")
    void countRejectedTransactionsByAccountId_callsRepository() {
        when(transactionRepository.countByAccountIdAndStatus(ACCOUNT_DB_ID, TransactionStatusEnum.REJECTED)).thenReturn(2);

        int count = transactionService.countRejectedTransactionsByAccountId(ACCOUNT_DB_ID);

        assertEquals(2, count);
        verify(transactionRepository).countByAccountIdAndStatus(ACCOUNT_DB_ID, TransactionStatusEnum.REJECTED);
    }
}