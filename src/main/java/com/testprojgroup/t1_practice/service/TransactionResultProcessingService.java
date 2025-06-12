package com.testprojgroup.t1_practice.service;


import com.testprojgroup.t1_practice.kafka.messages.TransactionResultMessage;

public interface TransactionResultProcessingService {
    void processResult(TransactionResultMessage message);
}