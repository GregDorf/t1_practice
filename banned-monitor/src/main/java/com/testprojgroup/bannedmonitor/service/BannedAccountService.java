package com.testprojgroup.bannedmonitor.service;

import com.testprojgroup.bannedmonitor.model.UnblockAccountResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface BannedAccountService {
    public UnblockAccountResponse decideAccountUnlock(UUID accountId);
}
