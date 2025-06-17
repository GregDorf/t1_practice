package com.testprojgroup.transactionmonitor.kafka;

import com.testprojgroup.transactionmonitor.kafka.messages.TransactionResultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionKafkaProducer {

    private final KafkaTemplate<String, TransactionResultMessage> kafkaTemplate;

    public void sendAccepted(UUID transactionId, UUID accountId) {
        kafkaTemplate.send("t1_demo_transaction_result",
                new TransactionResultMessage(transactionId, accountId, "ACCEPTED"));
    }

    public void sendRejected(UUID transactionId, UUID accountId) {
        kafkaTemplate.send("t1_demo_transaction_result",
                new TransactionResultMessage(transactionId, accountId, "REJECTED"));
    }

    public void sendBlocked(UUID transactionId, UUID accountId) {
        kafkaTemplate.send("t1_demo_transaction_result",
                new TransactionResultMessage(transactionId, accountId, "BLOCKED"));
    }
}
