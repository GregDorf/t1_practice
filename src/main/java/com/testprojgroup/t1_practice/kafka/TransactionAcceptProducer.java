package com.testprojgroup.t1_practice.kafka;

import com.testprojgroup.t1_practice.kafka.messages.TransactionAcceptMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionAcceptProducer {

    private final KafkaTemplate<String, TransactionAcceptMessage> kafkaTemplate;
    private static final String TOPIC = "t1_demo_transaction_accept";

    public void sendAcceptMessage(TransactionAcceptMessage message) {
        kafkaTemplate.send(TOPIC, message);
    }
}