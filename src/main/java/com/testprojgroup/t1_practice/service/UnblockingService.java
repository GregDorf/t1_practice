package com.testprojgroup.t1_practice.service;

import com.testprojgroup.t1_practice.model.UnblockAccountResponse;
import com.testprojgroup.t1_practice.model.UnblockClientResponse;

import java.util.UUID;

public interface UnblockingService {
    public UnblockClientResponse requestClientUnblock(UUID clientId);
    public UnblockAccountResponse requestAccountArrestRelease(UUID accountId);
}
