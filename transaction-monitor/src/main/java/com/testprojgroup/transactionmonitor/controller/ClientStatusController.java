package com.testprojgroup.transactionmonitor.controller;

import com.testprojgroup.transactionmonitor.model.ClientStatusResponse;
import com.testprojgroup.transactionmonitor.service.ClientStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client-status")
@RequiredArgsConstructor
public class ClientStatusController {

    private final ClientStatusService clientStatusService;

    @GetMapping
    public ClientStatusResponse getClientStatus(@RequestParam String clientId, @RequestParam String accountId) {
        boolean blacklisted = clientStatusService.isClientBlacklisted(clientId);

        String status = blacklisted ? "BLACKLISTED" : "OK";

        return new ClientStatusResponse(clientId, accountId, status);
    }
}