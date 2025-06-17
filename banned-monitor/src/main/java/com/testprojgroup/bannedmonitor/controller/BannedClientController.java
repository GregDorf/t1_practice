package com.testprojgroup.bannedmonitor.controller;

import com.testprojgroup.bannedmonitor.model.UnblockClientResponse;
import com.testprojgroup.bannedmonitor.service.BannedClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/unlock/clients")
public class BannedClientController {

    private final BannedClientService clientUnlockService;

    @Autowired
    public BannedClientController(BannedClientService bannedClientService) {
        this.clientUnlockService = bannedClientService;
    }

    @PostMapping("/{clientId}")
    public ResponseEntity<UnblockClientResponse> processClientUnlockDecision(@PathVariable UUID clientId) {
        UnblockClientResponse response = clientUnlockService.decideClientUnlock(clientId);
        return ResponseEntity.ok(response);
    }
}