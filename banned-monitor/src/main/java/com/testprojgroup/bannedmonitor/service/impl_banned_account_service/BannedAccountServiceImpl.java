package com.testprojgroup.bannedmonitor.service.impl_banned_account_service;

import com.testprojgroup.bannedmonitor.model.UnblockAccountResponse;
import com.testprojgroup.bannedmonitor.service.BannedAccountService;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class BannedAccountServiceImpl implements BannedAccountService {
    private final Random random = new Random();

    public UnblockAccountResponse decideAccountUnlock(UUID accountId) {
        boolean decision = random.nextInt(10) < 7;
        return new UnblockAccountResponse(accountId, decision);
    }
}