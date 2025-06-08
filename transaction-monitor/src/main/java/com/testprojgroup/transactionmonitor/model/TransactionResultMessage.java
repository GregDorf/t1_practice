package com.testprojgroup.transactionmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResultMessage {
    private UUID transactionId;
    private UUID accountId;
    private String status;
    private List<UUID> blockedTransactionIds;

    public TransactionResultMessage(UUID transactionId, UUID accountId, String status) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.status = status;
    }
}

