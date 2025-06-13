package com.testprojgroup.transactionmonitor.service;

import com.testprojgroup.transactionmonitor.kafka.messages.TransactionAcceptMessage;

public interface MonitorService {
    void handle(TransactionAcceptMessage msg);
}
