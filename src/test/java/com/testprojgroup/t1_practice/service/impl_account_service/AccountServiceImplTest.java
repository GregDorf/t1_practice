package com.testprojgroup.t1_practice.service.impl_account_service;

import com.testprojgroup.t1_practice.model.*;
import com.testprojgroup.t1_practice.repository.AccountRepository;
import com.testprojgroup.t1_practice.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ClientRepository clientRepository;
    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;
    private Client testClient;
    private UUID testAccountUuid;
    private UUID testClientUuid;
    private final Long DB_ID = 1L;

    @BeforeEach
    void setUp() {
        testClientUuid = UUID.randomUUID();
        testClient = Client.builder()
                .clientId(testClientUuid)
                .name("Test")
                .surname("Client")
                .status(ClientStatusEnum.UNBLOCKED)
                .build();
        ReflectionTestUtils.setField(testClient, "id", DB_ID);

        testAccountUuid = UUID.randomUUID();
        testAccount = Account.builder()
                .client(testClient)
                .account(AccountTypeEnum.DEBIT)
                .status(AccountStatusEnum.OPEN)
                .balance(1000.0)
                .accountId(testAccountUuid)
                .frozenAmount(0L)
                .build();
        ReflectionTestUtils.setField(testAccount, "id", DB_ID);
    }

    @Test
    @DisplayName("getAllAccounts - success")
    void getAllAccounts_success() {
        List<Account> expectedAccounts = Collections.singletonList(testAccount);
        when(accountRepository.findAll()).thenReturn(expectedAccounts);

        List<Account> actualAccounts = accountService.getAllAccounts();

        assertEquals(expectedAccounts, actualAccounts);
        verify(accountRepository).findAll();
    }

    @Test
    @DisplayName("getAccount by DB ID - success")
    void getAccount_byDbId_success() {
        when(accountRepository.getAccountById(DB_ID)).thenReturn(testAccount);
        Account found = accountService.getAccount(DB_ID);
        assertEquals(testAccount, found);
        verify(accountRepository).getAccountById(DB_ID);
    }

    @Test
    @DisplayName("saveAccount - success")
    void saveAccount_success() {
        accountService.saveAccount(testAccount);
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("deleteAccount by DB ID - success")
    void deleteAccount_byDbId_success() {
        accountService.deleteAccount(DB_ID);
        verify(accountRepository).deleteAccountById(DB_ID);
    }


    @Test
    @DisplayName("findByAccountId (UUID) - success")
    void findByAccountId_success() {
        when(accountRepository.findByAccountId(testAccountUuid)).thenReturn(Optional.of(testAccount));
        Account found = accountService.findByAccountId(testAccountUuid);
        assertEquals(testAccount, found);
        assertEquals(testAccountUuid, found.getAccountId());
    }

    @Test
    @DisplayName("findByAccountId (UUID) - not found throws RuntimeException")
    void findByAccountId_notFound_throwsException() {
        when(accountRepository.findByAccountId(testAccountUuid)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> accountService.findByAccountId(testAccountUuid));
        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    @DisplayName("adjustBalance - success")
    void adjustBalance_success() {
        double initialBalance = testAccount.getBalance();
        BigDecimal delta = new BigDecimal("50.75");
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.adjustBalance(testAccount, delta);

        assertEquals(initialBalance + delta.doubleValue(), testAccount.getBalance(), 0.001);
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("blockAccountAndClient - success")
    void blockAccountAndClient_success() {
        when(accountRepository.findByAccountId(testAccountUuid)).thenReturn(Optional.of(testAccount));
        when(clientRepository.findByClientId(testClientUuid)).thenReturn(Optional.of(testClient));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        accountService.blockAccountAndClient(testAccountUuid, testClientUuid);

        assertEquals(AccountStatusEnum.BLOCKED, testAccount.getStatus());
        assertEquals(ClientStatusEnum.BLOCKED, testClient.getStatus());
        verify(accountRepository).save(testAccount);
        verify(clientRepository).save(testClient);
    }

    @Test
    @DisplayName("blockAccountAndClient - account not found throws NoSuchElementException")
    void blockAccountAndClient_accountNotFound_throwsException() {
        when(accountRepository.findByAccountId(testAccountUuid)).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> accountService.blockAccountAndClient(testAccountUuid, testClientUuid));
    }

    @Test
    @DisplayName("updateAccountStatus - success")
    void updateAccountStatus_success() {
        AccountStatusEnum newStatus = AccountStatusEnum.ARRESTED;
        when(accountRepository.findByAccountId(testAccountUuid)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.updateAccountStatus(testAccountUuid, newStatus);
        assertEquals(newStatus, testAccount.getStatus());
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("updateAccountStatus - account not found throws RuntimeException")
    void updateAccountStatus_accountNotFound_throwsException() {
        AccountStatusEnum newStatus = AccountStatusEnum.CLOSED;
        when(accountRepository.findByAccountId(testAccountUuid)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> accountService.updateAccountStatus(testAccountUuid, newStatus));
        assertEquals("Account not found", exception.getMessage());
    }
}