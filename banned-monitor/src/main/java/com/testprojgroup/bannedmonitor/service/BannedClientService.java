package com.testprojgroup.bannedmonitor.service;

import com.testprojgroup.bannedmonitor.model.UnblockClientResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface BannedClientService {
    public UnblockClientResponse decideClientUnlock(UUID clientId);
}
