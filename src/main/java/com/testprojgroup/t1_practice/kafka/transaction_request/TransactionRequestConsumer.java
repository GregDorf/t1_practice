package com.testprojgroup.t1_practice.kafka.transaction_request;

import com.testprojgroup.t1_practice.kafka.messages.TransactionRequestMessage;
import com.testprojgroup.t1_practice.service.TransactionProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionRequestConsumer {

    private final TransactionProcessingService processingService;

    @KafkaListener(topics = "t1_demo_transactions", groupId = "transaction-service")
    public void handleTransactionRequest(TransactionRequestMessage msg) {
        processingService.processTransaction(msg);
    }
}