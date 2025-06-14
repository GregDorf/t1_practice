package com.testprojgroup.transactionmonitor.service.impl_client_status_service;

import com.testprojgroup.transactionmonitor.service.ClientStatusService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ClientStatusServiceImpl implements ClientStatusService {

    private final Random random = new Random();

    @Override
    public boolean isClientBlacklisted(String clientId) {
        // 10% клиентов считаются в черном списке
        return random.nextDouble() < 0.1;
    }
}