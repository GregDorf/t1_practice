package com.testprojgroup.transactionmonitor.service.impl_monitor_service;

import com.testprojgroup.transactionmonitor.config.MonitorProperties;
import com.testprojgroup.transactionmonitor.kafka.messages.TransactionAcceptMessage;
import com.testprojgroup.transactionmonitor.kafka.TransactionKafkaProducer;
import com.testprojgroup.transactionmonitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {

    private final MonitorProperties props;
    private final TransactionKafkaProducer kafkaProducer;

    private final Map<String, List<LocalDateTime>> history = new ConcurrentHashMap<>();

    public void handle(TransactionAcceptMessage msg) {
        String key = msg.getClientId().toString() + "_" + msg.getAccountId().toString();
        LocalDateTime now = msg.getTimestamp();

        history.putIfAbsent(key, new ArrayList<>());
        List<LocalDateTime> timestamps = history.get(key);
        timestamps.add(now);

        timestamps.removeIf(t -> t.isBefore(now.minus(props.getTimeWindow())));

        if (timestamps.size() > props.getThresholdCount()) {
            kafkaProducer.sendBlocked(msg.getTransactionId(), msg.getAccountId());
            return;
        }

        if (msg.getAmount().compareTo(BigDecimal.valueOf(msg.getBalance())) > 0) {
            kafkaProducer.sendRejected(msg.getTransactionId(), msg.getAccountId());
        } else {
            kafkaProducer.sendAccepted(msg.getTransactionId(), msg.getAccountId());
        }
    }
}