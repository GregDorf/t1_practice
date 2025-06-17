package com.testprojgroup.bannedmonitor.controller;

import com.testprojgroup.bannedmonitor.model.UnblockAccountResponse;
import com.testprojgroup.bannedmonitor.service.BannedAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/unlock/accounts")
public class BannedAccountController {

    private final BannedAccountService accountUnlockService;

    @Autowired
    public BannedAccountController(BannedAccountService bannedAccountService) {
        this.accountUnlockService = bannedAccountService;
    }

    @PostMapping("/{accountId}")
    public ResponseEntity<UnblockAccountResponse> processAccountUnlockDecision(@PathVariable UUID accountId) {
        UnblockAccountResponse response = accountUnlockService.decideAccountUnlock(accountId);
        return ResponseEntity.ok(response);
    }
}