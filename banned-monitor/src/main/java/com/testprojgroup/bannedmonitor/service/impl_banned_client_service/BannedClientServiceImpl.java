package com.testprojgroup.bannedmonitor.service.impl_banned_client_service;

import com.testprojgroup.bannedmonitor.model.UnblockClientResponse;
import com.testprojgroup.bannedmonitor.service.BannedClientService;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class BannedClientServiceImpl implements BannedClientService {
    private final Random random = new Random();

    public UnblockClientResponse decideClientUnlock(UUID clientId) {
        boolean decision = random.nextBoolean();
        return new UnblockClientResponse(clientId, decision);
    }
}