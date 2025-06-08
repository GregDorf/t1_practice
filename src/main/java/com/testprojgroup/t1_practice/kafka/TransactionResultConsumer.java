package com.testprojgroup.t1_practice.kafka;

import com.testprojgroup.t1_practice.service.TransactionResultProcessingService;
import com.testprojgroup.transactionmonitor.model.TransactionResultMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionResultConsumer {

    private final TransactionResultProcessingService transactionResultProcessingService;

    @KafkaListener(topics = "t1_demo_transaction_result", groupId = "t1-practice-group")
    public void handleTransactionResult(TransactionResultMessage message) {
        transactionResultProcessingService.processResult(message);
    }
}