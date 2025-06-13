package com.testprojgroup.t1_practice.kafka.messages;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAcceptMessage {
    private UUID clientId;
    private UUID accountId;
    private UUID transactionId;
    private Long timestamp;
    private BigDecimal amount;
    private double accountBalance;
}