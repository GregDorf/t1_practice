package com.testprojgroup.t1_practice.repository;

import com.testprojgroup.t1_practice.model.Transaction;
import com.testprojgroup.t1_practice.model.TransactionStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    int countByAccountIdAndStatus(Long accountId, TransactionStatusEnum status);

    Transaction getTransactionById(Long id);

    Optional<Transaction> findByTransactionId(UUID transactionId);

    List<Transaction> findAllByTransactionIdIn(List<UUID> transactionIds);
}
