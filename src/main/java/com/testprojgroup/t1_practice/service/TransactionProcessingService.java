package com.testprojgroup.t1_practice.service;

import com.testprojgroup.t1_practice.kafka.messages.TransactionRequestMessage;

public interface TransactionProcessingService {
    public void processTransaction(TransactionRequestMessage transaction);
}
