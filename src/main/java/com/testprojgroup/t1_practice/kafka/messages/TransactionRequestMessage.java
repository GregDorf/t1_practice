package com.testprojgroup.t1_practice.kafka.messages;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestMessage {
    private UUID clientId;
    private UUID accountId;
    private BigDecimal amount;
}