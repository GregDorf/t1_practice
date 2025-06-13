package com.testprojgroup.transactionmonitor.kafka;

import com.testprojgroup.transactionmonitor.model.TransactionAcceptMessage;
import com.testprojgroup.transactionmonitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionAcceptListener {

    private final MonitorService monitorService;

    @KafkaListener(topics = "t1_demo_transaction_accept", groupId = "monitor-group")
    public void listen(ConsumerRecord<String, TransactionAcceptMessage> record) {
        monitorService.handle(record.value());
    }
}

