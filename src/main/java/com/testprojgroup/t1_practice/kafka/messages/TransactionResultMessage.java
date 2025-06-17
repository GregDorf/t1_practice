package com.testprojgroup.t1_practice.kafka.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
