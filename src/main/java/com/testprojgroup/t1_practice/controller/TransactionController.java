package com.testprojgroup.t1_practice.controller;

import com.testprojgroup.t1_practice.model.Transaction;
import com.testprojgroup.t1_practice.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("transactions")
@AllArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("")
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public Transaction getTransaction(@PathVariable Long id) {
        return transactionService.getTransaction(id);
    }

    @PostMapping("save")
    public void save(@RequestBody Transaction transaction) {
        transactionService.createTransaction(transaction);
    }

    @DeleteMapping("delete/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}