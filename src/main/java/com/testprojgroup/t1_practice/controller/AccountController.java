package com.testprojgroup.t1_practice.controller;

import com.testprojgroup.t1_practice.model.Account;
import com.testprojgroup.t1_practice.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("accounts")
@AllArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @PostMapping("/register")
    public void saveAccount(@RequestBody Account account) {
        accountService.saveAccount(account);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}
