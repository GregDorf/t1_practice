package com.testprojgroup.t1_practice.repository;

import com.testprojgroup.t1_practice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction getTransactionById(Long id);

    Optional<Transaction> findByTransactionId(UUID transactionId);

    List<Transaction> findAllByTransactionIdIn(List<UUID> transactionIds);
}
