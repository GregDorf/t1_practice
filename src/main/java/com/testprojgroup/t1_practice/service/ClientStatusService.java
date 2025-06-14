package com.testprojgroup.t1_practice.service;

import com.testprojgroup.t1_practice.model.ClientStatusResponse;

import java.util.UUID;

public interface ClientStatusService {
    public ClientStatusResponse fetchStatusFromService2(UUID clientId, UUID accountId);
}
