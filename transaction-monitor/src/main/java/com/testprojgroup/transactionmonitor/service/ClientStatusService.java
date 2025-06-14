package com.testprojgroup.transactionmonitor.service;

public interface ClientStatusService {
    boolean isClientBlacklisted(String clientId);
}